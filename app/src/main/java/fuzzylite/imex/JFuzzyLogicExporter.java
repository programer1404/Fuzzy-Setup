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
import fuzzylite.Op;
import fuzzylite.rule.RuleBlock;
import fuzzylite.term.*;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;

import java.io.StringWriter;
import java.text.MessageFormat;

public class JFuzzyLogicExporter extends Exporter {

    @Override
    public String toString(Engine engine) {
        StringWriter writer = new StringWriter();

        writer.append("FIS fis = new FIS();\n");
        writer.append("FunctionBlock engine = new FunctionBlock(fis);\n");
        writer.append("fis.addFunctionBlock(\"engine\", engine);\n");

        for (InputVariable inputVariable : engine.getInputVariables()) {
            writer.append(toString(inputVariable)).append("\n");
        }

        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            writer.append(toString(outputVariable)).append("\n");
        }

        for (RuleBlock ruleBlock : engine.getRuleBlocks()) {
            writer.append(toString(ruleBlock)).append("\n");
        }
        return writer.toString();
    }

    public String toString(InputVariable inputVariable) {
        StringWriter writer = new StringWriter();
        writer.append(MessageFormat.format(
                "Variable {0} = new Variable(\"{0}\");\n",
                inputVariable.getName()));

        for (Term term : inputVariable.getTerms()) {
            writer.append(toString(term)).append("\n");
            writer.append(MessageFormat.format(
                    "{0}.add({1});\n",
                    new Object[]{inputVariable.getName(), term.getName()}));
        }

        writer.append(MessageFormat.format(
                "engine.setVariable({0}.getName(), {0});\n",
                inputVariable.getName()));

        return "";
    }

    public String toString(OutputVariable outputVariable) {
        return "";
    }

    public String toString(Term term) {
        StringWriter writer = new StringWriter();
        writer.append("//Linguistic Term: " + term.getName() + "\n");
        String membershipFunction;
        if (term instanceof Cosine) {
            Cosine cosine = (Cosine) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionCosine({0}, {1})",
                    Op.str(cosine.getWidth()), Op.str(cosine.getCenter()));

        } else if (term instanceof SigmoidDifference) {
            SigmoidDifference sigmoidDifference = (SigmoidDifference) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionDifferenceSigmoidal({0}, {1}, {2}, {3})",
                    Op.str(sigmoidDifference.getRising()), Op.str(sigmoidDifference.getLeft()),
                    Op.str(sigmoidDifference.getFalling()), Op.str(sigmoidDifference.getRight()));

        } else if (term instanceof Gaussian) {
            Gaussian gaussian = (Gaussian) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionGaussian({0}, {1})",
                    Op.str(gaussian.getMean()), Op.str(gaussian.getStandardDeviation()));

        } else if (term instanceof GaussianProduct) {
            GaussianProduct gaussianProduct = (GaussianProduct) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionGaussian2({0}, {1}, {2}, {3})",
                    Op.str(gaussianProduct.getMeanA()), Op.str(gaussianProduct.getStandardDeviationA()),
                    Op.str(gaussianProduct.getMeanB()), Op.str(gaussianProduct.getStandardDeviationB()));

        } else if (term instanceof Bell) {
            Bell bell = (Bell) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionGenBell({0}, {1}, {2})",
                    Op.str(bell.getCenter()), Op.str(bell.getWidth()), Op.str(bell.getSlope()));

        } else if (term instanceof Sigmoid) {
            Sigmoid sigmoid = (Sigmoid) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionSigmoidal({0}, {1})",
                    Op.str(sigmoid.getSlope()), Op.str(sigmoid.getInflection()));

        } else if (term instanceof Trapezoid) {
            Trapezoid trapezoid = (Trapezoid) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionTrapetzoidal({0}, {1}, {2}, {3})",
                    Op.str(trapezoid.getVertexA()), Op.str(trapezoid.getVertexB()),
                    Op.str(trapezoid.getVertexC()), Op.str(trapezoid.getVertexD()));

        } else if (term instanceof Triangle) {
            Triangle triangle = (Triangle) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionTriangular({0}, {1}, {2})",
                    Op.str(triangle.getVertexA()), Op.str(triangle.getVertexB()), Op.str(triangle.getVertexC()));

        } else if (term instanceof Discrete) {
            Discrete discrete = (Discrete) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionPieceWiseLinear({0}, {1})",
                    discrete.getXY());

        } else if (term instanceof Constant) {
            Constant constant = (Constant) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionSingleton(new Value({0}))",
                    Op.str(constant.getValue()));

        } else if (term instanceof Linear) {
            Linear linear = (Linear) term;
            membershipFunction = MessageFormat.format(
                    "new MembershipFunctionFuncion(engine, new Value({0}))",
                    linear.getCoefficients());
        }

        writer.append(MessageFormat.format(
                "LinguisticTerm {0} = new LinguisticTerm(\"{0}\", mf{0});\n",
                term.getName()));
        return writer.toString();
    }

    public String toString(RuleBlock ruleBlock) {
        return "";
    }

}
