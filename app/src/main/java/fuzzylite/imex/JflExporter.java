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
import fuzzylite.factory.FactoryManager;
import fuzzylite.norm.Norm;
import fuzzylite.norm.SNorm;
import fuzzylite.norm.s.*;
import fuzzylite.norm.t.*;
import fuzzylite.rule.Rule;
import fuzzylite.rule.RuleBlock;
import fuzzylite.term.*;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;
import fuzzylite.variable.Variable;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static fuzzylite.Op.str;

public class JflExporter extends Exporter {

    private String indent;

    public JflExporter() {
        this("  ");
    }

    public JflExporter(String indent) {
        this.indent = indent;
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    @Override
    public String toString(Engine engine) {
        StringBuilder result = new StringBuilder();

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

        Set<String> aggregationOperators = new HashSet<String>();
        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            result.append(toString(outputVariable)).append("\n");
            SNorm aggregation = outputVariable.fuzzyOutput().getAggregation();
            if (aggregation != null) {
                aggregationOperators.add(aggregation.getClass().getSimpleName());
            }
        }

        for (RuleBlock ruleBlock : engine.getRuleBlocks()) {
            if (aggregationOperators.size() > 1) {
                throw new RuntimeException("FCL supports only a single aggregation operator,"
                        + " but found <" + aggregationOperators.size() + ">: " + Op.join(aggregationOperators, " "));
            }
            SNorm aggregation = null;
            if (!aggregationOperators.isEmpty()) {
                aggregation = FactoryManager.instance().snorm().constructObject(
                        aggregationOperators.iterator().next());
            }
            result.append(toString(ruleBlock, aggregation)).append("\n");
        }

        result.append("END_FUNCTION_BLOCK\n");

        return result.toString();
    }

    public String toString(InputVariable inputVariable) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("FUZZIFY %s\n", inputVariable.getName()));
//        result.append(String.format(indent + "RANGE := (%s .. %s);\n",
//                Op.str(inputVariable.getMinimum()), Op.str(inputVariable.getMaximum())));

        for (Term term : inputVariable.getTerms()) {
            result.append(String.format(indent + "TERM %s := %s;\n",
                    term.getName(), toString(term, inputVariable)));
        }
        result.append("END_FUZZIFY\n");
        return result.toString();
    }

    public String toString(OutputVariable outputVariable) {
        StringBuilder result = new StringBuilder();

        result.append(String.format("DEFUZZIFY %s\n", outputVariable.getName()));
//        result.append(String.format(indent + "RANGE := (%s .. %s);\n",
//                Op.str(outputVariable.getMinimum()), Op.str(outputVariable.getMaximum())));
        for (Term term : outputVariable.getTerms()) {
            result.append(String.format(indent + "TERM %s := %s;\n",
                    term.getName(), toString(term, outputVariable)));
        }
        if (outputVariable.getDefuzzifier() != null) {
            result.append(String.format(indent + "METHOD : %s;\n",
                    toString(outputVariable.getDefuzzifier())));
        }
//        if (outputVariable.fuzzyOutput().getAggregation() != null) {
//            result.append(String.format(indent + "ACCU : %s;\n",
//                    toString(outputVariable.fuzzyOutput().getAggregation())));
//        }
        double defaultValue = outputVariable.getDefaultValue();
        if (Double.isNaN(defaultValue) || Double.isInfinite(defaultValue)) {
            defaultValue = 0.0;
        }
        result.append(String.format(indent + "DEFAULT := %s",
                str(defaultValue)));
//        if (outputVariable.isLockPreviousValue()) {
//            result.append(" | NC");
//        }
        result.append(";\n");
        result.append("END_DEFUZZIFY\n");
        return result.toString();
    }

    public String toString(RuleBlock ruleBlock, SNorm aggregation) {
        StringWriter result = new StringWriter();
        String name = ruleBlock.getName();
        if (name.trim().isEmpty()) {
            name = "No1";
        }
        result.append(String.format("RULEBLOCK %s\n", name));
        if (ruleBlock.getConjunction() != null) {
            result.append(String.format(indent + "AND : %s;\n", toString(ruleBlock.getConjunction())));
        }
        if (ruleBlock.getDisjunction() != null) {
            result.append(String.format(indent + "OR : %s;\n", toString(ruleBlock.getDisjunction())));
        }
        if (ruleBlock.getImplication() != null) {
            result.append(String.format(indent + "ACT : %s;\n", toString(ruleBlock.getImplication())));
        }
        if (aggregation != null) {
            result.append(String.format(indent + "ACCU : %s;\n",
                    toString(aggregation)));
        }

        int index = 0;
        for (Rule rule : ruleBlock.getRules()) {
            ++index;
            String text = rule.getText();
            text = text.replaceFirst("\\s*" + Rule.FL_IF + "\\s+", " " + Rule.FL_IF.toUpperCase() + " ");
            text = text.replaceAll("\\s+" + Rule.FL_IS + "\\s+", " " + Rule.FL_IS.toUpperCase() + " ");
            text = text.replaceAll("\\s+" + Rule.FL_AND + "\\s+", " " + Rule.FL_AND.toUpperCase() + " ");
            text = text.replaceAll("\\s+" + Rule.FL_OR + "\\s+", " " + Rule.FL_OR.toUpperCase() + " ");
            text = text.replaceFirst("\\s+" + Rule.FL_THEN + "\\s+", " " + Rule.FL_THEN.toUpperCase() + " ");
            text = text.replaceFirst("\\s+" + Rule.FL_WITH + "\\s+", " " + Rule.FL_WITH.toUpperCase() + " ");

            String[] tokens = text.split(Pattern.quote(" " + Rule.FL_THEN.toUpperCase() + " "));
            if (tokens.length != 2) {
                throw new RuntimeException("Expected antecedent THEN consequent, but found: " + text);
            }
            String antecedent = tokens[0];
            String[] consequents = tokens[1].split(Pattern.quote(" " + Rule.FL_AND.toUpperCase() + " "));

            for (String consequent : consequents) {
                result.append(indent + "RULE " + index + " : "
                        + antecedent + " " + Rule.FL_THEN.toUpperCase() + " "
                        + consequent + " ;\n");
            }

        }
        result.append("END_RULEBLOCK\n");
        return result.toString();
    }

    //@todo: create t-norm and s-norm versions ?only?
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

    public String toString(Term term, Variable variable) {
        if (term == null) {
            return "";
        }
        String membershipFunction;
        if (term instanceof Cosine) {
            Cosine cosine = (Cosine) term;
            membershipFunction = MessageFormat.format(
                    "COSINE {0} {1}",
                    Op.str(cosine.getWidth()), Op.str(cosine.getCenter()));

        } else if (term instanceof SigmoidDifference) {
            SigmoidDifference sigmoidDifference = (SigmoidDifference) term;
            membershipFunction = MessageFormat.format(
                    "DSIGM {0} {1} {2} {3}",
                    Op.str(sigmoidDifference.getRising()), Op.str(sigmoidDifference.getLeft()),
                    Op.str(sigmoidDifference.getFalling()), Op.str(sigmoidDifference.getRight()));

        } else if (term instanceof Gaussian) {
            Gaussian gaussian = (Gaussian) term;
            membershipFunction = MessageFormat.format(
                    "GAUSS {0} {1}",
                    Op.str(gaussian.getMean()), Op.str(gaussian.getStandardDeviation()));

        } else if (term instanceof GaussianProduct) {
            GaussianProduct gaussianProduct = (GaussianProduct) term;
            membershipFunction = MessageFormat.format(
                    "GAUSS2 {0} {1} {2} {3}",
                    Op.str(gaussianProduct.getMeanA()), Op.str(gaussianProduct.getStandardDeviationA()),
                    Op.str(gaussianProduct.getMeanB()), Op.str(gaussianProduct.getStandardDeviationB()));

        } else if (term instanceof Bell) {
            Bell bell = (Bell) term;
            membershipFunction = MessageFormat.format(
                    " GBELL {0} {1} {2}",
                    Op.str(bell.getWidth()), Op.str(bell.getSlope()), Op.str(bell.getCenter()));

        } else if (term instanceof Sigmoid) {
            Sigmoid sigmoid = (Sigmoid) term;
            membershipFunction = MessageFormat.format(
                    "SIGM {0} {1}",
                    Op.str(sigmoid.getSlope()), Op.str(sigmoid.getInflection()));

        } else if (term instanceof Trapezoid) {
            Trapezoid trapezoid = (Trapezoid) term;
            membershipFunction = MessageFormat.format(
                    "TRAPE {0} {1} {2} {3}",
                    Op.str(trapezoid.getVertexA()), Op.str(trapezoid.getVertexB()),
                    Op.str(trapezoid.getVertexC()), Op.str(trapezoid.getVertexD()));

        } else if (term instanceof Triangle) {
            Triangle triangle = (Triangle) term;
            membershipFunction = MessageFormat.format(
                    "TRIAN {0} {1} {2}",
                    Op.str(triangle.getVertexA()), Op.str(triangle.getVertexB()), Op.str(triangle.getVertexC()));

        } else if (term instanceof Ramp) {
            Ramp ramp = (Ramp) term;
            if (ramp.direction() == Ramp.Direction.Positive) {
                membershipFunction = MessageFormat.format(
                        "TRIAN {0} {1} {2}",
                        Op.str(ramp.getStart()), Op.str(ramp.getEnd()), Op.str(ramp.getEnd()));
            } else if (ramp.direction() == Ramp.Direction.Negative) {
                membershipFunction = MessageFormat.format(
                        "TRIAN {0} {1} {2}",
                        Op.str(ramp.getEnd()), Op.str(ramp.getEnd()), Op.str(ramp.getStart()));
            } else {
                throw new RuntimeException("FCL not available for: " + term.toString());
            }
        } else if (term instanceof Discrete) {
            Discrete discrete = (Discrete) term;
            membershipFunction = Discrete.formatXY(discrete.getXY());

        } else if (term instanceof Function) {
            Function function = (Function) term;
            String formula = function.getFormula();
            //Replace function fabs() for abs()
            formula = formula.replaceAll("fabs\\(", "abs(");
            membershipFunction = "FUNCTION " + formula;

        } else if (term instanceof Constant) {
            Constant constant = (Constant) term;
            membershipFunction = MessageFormat.format(
                    " {0}", Op.str(constant.getValue()));

        } else if (term instanceof Linear) {
            Linear linear = (Linear) term;
            StringWriter function = new StringWriter();
            Engine engine = linear.getEngine();
            List<Double> coefficients = linear.getCoefficients();
            for (int i = 0; i < coefficients.size() - 1; ++i) {
                function.append("(" + Op.str(coefficients.get(i)) + " * "
                        + engine.getInputVariable(i).getName() + ")");
                if (i + 1 < coefficients.size()) {
                    function.append(" + ");
                }
            }
            if (coefficients.size() > engine.getInputVariables().size()) {
                function.append("(" + Op.str(coefficients.get(coefficients.size() - 1)) + ")");
            }
            membershipFunction = "FUNCTION " + function.toString();

            /*
             * Discretization of terms not supported by jFuzzyLogic
             */
        } else {
            FuzzyLite.logger().log(Level.WARNING, "Discretizing {0}", term.getClass().getSimpleName());
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(term, variable.getMinimum(), variable.getMaximum(),
                            100)
                    .getXY());
        }
        /*else if (term instanceof Concave) {
            Concave concave = (Concave) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(concave, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else if (term instanceof PiShape) {
            PiShape piShape = (PiShape) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(piShape, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else if (term instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) term;
            membershipFunction = MessageFormat.format(
                    "({0}, {1}) ({2}, {3})",
                    Op.str(rectangle.getStart()), Op.str(1.0),
                    Op.str(rectangle.getEnd()), Op.str(1.0));

        } else if (term instanceof SigmoidProduct) {
            SigmoidProduct sigmoidProduct = (SigmoidProduct) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(sigmoidProduct, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else if (term instanceof Spike) {
            Spike spike = (Spike) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(spike, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else if (term instanceof SShape) {
            SShape sshape = (SShape) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(sshape, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else if (term instanceof ZShape) {
            ZShape zshape = (ZShape) term;
            membershipFunction = Discrete.formatXY(Discrete
                    .discretize(zshape, variable.getMinimum(), variable.getMaximum(),
                            IntegralDefuzzifier.getDefaultResolution())
                    .getXY());

        } else {
            throw new RuntimeException("FCL not available for: " + term.toString());
        }
         */
        return membershipFunction;
    }

    @Override
    public JflExporter clone() throws CloneNotSupportedException {
        return (JflExporter) super.clone();
    }

}
