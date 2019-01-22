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
import fuzzylite.hedge.*;
import fuzzylite.norm.SNorm;
import fuzzylite.norm.TNorm;
import fuzzylite.norm.s.*;
import fuzzylite.norm.t.*;
import fuzzylite.rule.*;
import fuzzylite.term.*;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;
import fuzzylite.variable.Variable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;

/**
 The FisExporter class is an Exporter that translates an Engine and its
 components into the Fuzzy Inference System format for Matlab or Octave.

 @author Juan Rada-Vilela, Ph.D.
 @see FisImporter
 @see Exporter
 @since 4.0
 */
public class FisExporter extends Exporter {

    public FisExporter() {

    }

    @Override
    public String toString(Engine engine) {
        String result =
                exportSystem(engine) + "\n" +
                exportInputs(engine) +
                exportOutputs(engine) +
                exportRules(engine);
        return result;
    }

    /**
     Returns a string representation of the `[System]` configuration

     @param engine is the engine
     @return a string representation of the `[System]` configuration
     */
    public String exportSystem(Engine engine) {
        StringBuilder result = new StringBuilder();
        result.append("#Code automatically generated with " + FuzzyLite.LIBRARY + ".\n\n");
        result.append("[System]\n");
        result.append(String.format("Name='%s'\n", engine.getName()));
        String type;
        if (engine.type() == Engine.Type.Mamdani || engine.type() == Engine.Type.Larsen) {
            type = "mamdani";
        } else if (engine.type() == Engine.Type.TakagiSugeno) {
            type = "sugeno";
        } else if (engine.type() == Engine.Type.Tsukamoto) {
            type = "tsukamoto";
        } else if (engine.type() == Engine.Type.InverseTsukamoto) {
            type = "inverse tsukamoto";
        } else if (engine.type() == Engine.Type.Hybrid) {
            type = "hybrid";
        } else {
            type = "unknown";
        }
        result.append(String.format("Type='%s'\n", type));
        result.append(String.format("Version=%s\n", FuzzyLite.VERSION));
        result.append(String.format("NumInputs=%d\n", engine.numberOfInputVariables()));
        result.append(String.format("NumOutputs=%d\n", engine.numberOfOutputVariables()));

        int numberOfRules = 0;
        TNorm conjunction = null;
        SNorm disjunction = null;
        TNorm implication = null;
        for (RuleBlock ruleBlock : engine.getRuleBlocks()) {
            numberOfRules += ruleBlock.numberOfRules();
            if (conjunction == null) {
                conjunction = ruleBlock.getConjunction();
            }
            if (disjunction == null) {
                disjunction = ruleBlock.getDisjunction();
            }
            if (implication == null) {
                implication = ruleBlock.getImplication();
            }
        }
        result.append(String.format("NumRules=%d\n", numberOfRules));
        result.append(String.format("AndMethod='%s'\n", toString(conjunction)));
        result.append(String.format("OrMethod='%s'\n", toString(disjunction)));
        result.append(String.format("ImpMethod='%s'\n", toString(implication)));

        SNorm accumulation = null;
        Defuzzifier defuzzifier = null;
        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            if (accumulation == null) {
                accumulation = outputVariable.fuzzyOutput().getAggregation();
            }
            if (defuzzifier == null) {
                defuzzifier = outputVariable.getDefuzzifier();
            }
        }
        result.append(String.format("AggMethod='%s'\n", toString(accumulation)));
        result.append(String.format("DefuzzMethod='%s'\n", toString(defuzzifier)));
        return result.toString();
    }

    /**
     Returns a string representation of the `[Input]` configuration

     @param engine is the engine
     @return a string representation of the `[Input]` configuration
     */
    public String exportInputs(Engine engine) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < engine.numberOfInputVariables(); ++i) {
            InputVariable inputVariable = engine.getInputVariable(i);
            result.append(String.format("[Input%d]\n", i + 1));
            result.append(String.format("Name='%s'\n", inputVariable.getName()));
            result.append(String.format("Range=[%s %s]\n",
                    Op.str(inputVariable.getMinimum()), Op.str(inputVariable.getMaximum())));
            result.append(String.format("NumMFs=%d\n", inputVariable.numberOfTerms()));
            for (int t = 0; t < inputVariable.numberOfTerms(); ++t) {
                Term term = inputVariable.getTerm(t);
                result.append(String.format("MF%d=%s\n",
                        t + 1, toString(term)));
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     Returns a string representation of the `[Output]` configuration

     @param engine is the engine
     @return a string representation of the `[Output]` configuration
     */
    public String exportOutputs(Engine engine) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < engine.numberOfOutputVariables(); ++i) {
            OutputVariable outputVariable = engine.getOutputVariable(i);
            result.append(String.format("[Output%d]\n", i + 1));
            result.append(String.format("Name='%s'\n", outputVariable.getName()));
            result.append(String.format("Range=[%s %s]\n",
                    Op.str(outputVariable.getMinimum()), Op.str(outputVariable.getMaximum())));
            result.append(String.format("NumMFs=%d\n", outputVariable.numberOfTerms()));
            for (int t = 0; t < outputVariable.numberOfTerms(); ++t) {
                Term term = outputVariable.getTerm(t);
                result.append(String.format("MF%d=%s\n",
                        t + 1, toString(term)));
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     Returns a string representation of the `[Rules]` configuration

     @param engine is the engine
     @return a string representation of the `[Rules]` configuration
     */
    public String exportRules(Engine engine) {
        StringBuilder result = new StringBuilder();
        result.append("[Rules]\n");
        for (RuleBlock ruleBlock : engine.getRuleBlocks()) {
            if (engine.numberOfRuleBlocks() > 1) {
                result.append(String.format("# RuleBlock %d",
                        1 + engine.getRuleBlocks().indexOf(ruleBlock)));
            }
            for (Rule rule : ruleBlock.getRules()) {
                if (rule.isLoaded()) {
                    result.append(exportRule(rule, engine)).append("\n");
                }
            }
        }
        return result.toString();
    }

    /**
     Returns a string representation for the Rule in the Fuzzy Inference System
     format

     @param rule is the rule
     @param engine is the engine in which the rule is registered
     @return a string representation for the rule in the Fuzzy Inference System
     format
     */
    public String exportRule(Rule rule, Engine engine) {
        List<Proposition> propositions = new ArrayList<Proposition>();
        List<Operator> operators = new ArrayList<Operator>();
        Deque<Expression> queue = new ArrayDeque<Expression>();

        queue.offer(rule.getAntecedent().getExpression());
        while (!queue.isEmpty()) {
            Expression front = queue.poll();
            if (front instanceof Operator) {
                Operator op = (Operator) front;
                queue.offer(op.getLeft());
                queue.offer(op.getRight());
                operators.add(op);
            } else if (front instanceof Proposition) {
                propositions.add((Proposition) front);
            } else {
                throw new RuntimeException(String.format(
                        "[export error] unexpected class <%s>", front.getClass().getName()));
            }
        }
        boolean equalOperators = true;
        for (int i = 0; i < operators.size() - 1; ++i) {
            if (!operators.get(i).getName().equals(operators.get(i + 1).getName())) {
                equalOperators = false;
                break;
            }
        }
        if (!equalOperators) {
            throw new RuntimeException("[export error] "
                    + "fis files do not support rules with different connectors "
                    + "(i.e. ['and', 'or']). All connectors within a rule must be the same");
        }
        List<Variable> inputVariables = new ArrayList<Variable>();
        List<Variable> outputVariables = new ArrayList<Variable>();
        for (InputVariable inputVariable : engine.getInputVariables()) {
            inputVariables.add(inputVariable);
        }
        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            outputVariables.add(outputVariable);
        }

        StringBuilder result = new StringBuilder();
        result.append(translate(propositions, inputVariables)).append(", ");
        result.append(translate(rule.getConsequent().getConclusions(), outputVariables));
        result.append(String.format("(%s)", Op.str(rule.getWeight())));
        String connector;
        if (operators.isEmpty()) {
            connector = "1";
        } else if (Rule.FL_AND.equals(operators.get(0).getName())) {
            connector = "1";
        } else if (Rule.FL_OR.equals(operators.get(0).getName())) {
            connector = "2";
        } else {
            connector = operators.get(0).getName();
        }
        result.append(" : ").append(connector);
        return result.toString();
    }

    protected String translate(List<Proposition> propositions, List<Variable> variables) {
        StringBuilder result = new StringBuilder();
        for (Variable variable : variables) {
            int termIndexPlusOne = 0;
            double plusHedge = 0;
            int negated = 1;
            for (Proposition proposition : propositions) {
                if (!variable.equals(proposition.getVariable())) {
                    continue;
                }
                for (int termIndex = 0; termIndex < variable.numberOfTerms(); ++termIndex) {
                    if (variable.getTerm(termIndex).equals(proposition.getTerm())) {
                        termIndexPlusOne = termIndex + 1;
                        break;
                    }
                }
                if (proposition.getHedges().size() > 1) {
                    FuzzyLite.logger().log(Level.FINE, "[export warning] "
                            + "only a few combinations of multiple "
                            + "hedges are supported in fis files");
                }
                for (Hedge hedge : proposition.getHedges()) {
                    if (hedge instanceof Not) {
                        negated *= -1;
                    } else if (hedge instanceof Extremely) {
                        plusHedge += 0.3;
                    } else if (hedge instanceof Very) {
                        plusHedge += 0.2;
                    } else if (hedge instanceof Somewhat) {
                        plusHedge += 0.05;
                    } else if (hedge instanceof Seldom) {
                        plusHedge += 0.01;
                    } else if (hedge instanceof Any) {
                        plusHedge += 0.99;
                    } else {
                        plusHedge = Double.NaN; // Unrecognized hedge combination
                    }
                }
                break;
            }

            if (negated < 0) {
                result.append("-");
            }
            if (!Double.isNaN(plusHedge)) {
                result.append(Op.str(termIndexPlusOne + plusHedge));
            } else { //Unrecognized hedge combination
                result.append(String.format("%d.?", termIndexPlusOne));
            }
            result.append(" ");
        }
        return result.toString();
    }

    /**
     Returns a string representation of the TNorm in the Fuzzy Inference System
     format

     @param tnorm is the TNorm
     @return a string representation of the TNorm in the Fuzzy Inference System
     format
     */
    public String toString(TNorm tnorm) {
        if (tnorm == null) {
            return "min";
        }
        if (tnorm instanceof Minimum) {
            return "min";
        }
        if (tnorm instanceof AlgebraicProduct) {
            return "prod";
        }
        if (tnorm instanceof BoundedDifference) {
            return "bounded_difference";
        }
        if (tnorm instanceof DrasticProduct) {
            return "drastic_product";
        }
        if (tnorm instanceof EinsteinProduct) {
            return "einstein_product";
        }
        if (tnorm instanceof HamacherProduct) {
            return "hamasher_product";
        }
        if (tnorm instanceof NilpotentMinimum) {
            return "nilpotent_minimum";
        }
        return tnorm.getClass().getSimpleName();
    }

    /**
     Returns a string representation of the SNorm in the Fuzzy Inference System
     format

     @param snorm is the SNorm
     @return a string representation of the SNorm in the Fuzzy Inference System
     format
     */
    public String toString(SNorm snorm) {
        if (snorm == null) {
            return "max";
        }
        if (snorm instanceof Maximum) {
            return "max";
        }
        if (snorm instanceof AlgebraicSum) {
            return "probor";
        }
        if (snorm instanceof NormalizedSum) {
            return "normalized_sum";
        }
        if (snorm instanceof BoundedSum) {
            return "bounded_sum";
        }
        if (snorm instanceof DrasticSum) {
            return "drastic_sum";
        }
        if (snorm instanceof EinsteinSum) {
            return "einstein_sum";
        }
        if (snorm instanceof HamacherSum) {
            return "hamacher_sum";
        }
        if (snorm instanceof NilpotentMaximum) {
            return "nilpotent_maximum";
        }
        if (snorm instanceof UnboundedSum) {
            return "sum";
        }
        return snorm.getClass().getSimpleName();
    }

    /**
     Returns a string representation of the Defuzzifier in the Fuzzy Inference
     System format

     @param defuzzifier is the defuzzifier
     @return a string representation of the Defuzzifier in the Fuzzy Inference
     System format
     */
    public String toString(Defuzzifier defuzzifier) {
        if (defuzzifier == null) {
            return "";
        }
        if (defuzzifier instanceof Centroid) {
            return "centroid";
        }
        if (defuzzifier instanceof Bisector) {
            return "bisector";
        }
        if (defuzzifier instanceof LargestOfMaximum) {
            return "lom";
        }
        if (defuzzifier instanceof MeanOfMaximum) {
            return "mom";
        }
        if (defuzzifier instanceof SmallestOfMaximum) {
            return "som";
        }
        if (defuzzifier instanceof WeightedAverage) {
            return "wtaver";
        }
        if (defuzzifier instanceof WeightedSum) {
            return "wtsum";
        }
        return defuzzifier.getClass().getSimpleName();
    }

    /**
     Returns a string representation of the Term in the Fuzzy Inference System
     format

     @param term is the term
     @return a string representation of the term in the Fuzzy Inference System
     format
     */
    public String toString(Term term) {
        if (term instanceof Bell) {
            Bell t = (Bell) term;
            return String.format("'%s':'gbellmf',[%s]", term.getName(),
                    Op.join(" ", t.getWidth(), t.getSlope(), t.getCenter()));
        }

        if (term instanceof Binary) {
            Binary t = (Binary) term;
            return String.format("'%s':'binarymf',[%s]", term.getName(),
                    Op.join(" ", t.getStart(), t.getDirection()));
        }

        if (term instanceof Concave) {
            Concave t = (Concave) term;
            return String.format("'%s':'concavemf',[%s]", term.getName(),
                    Op.join(" ", t.getInflection(), t.getEnd()));
        }
        if (term instanceof Constant) {
            Constant t = (Constant) term;
            return String.format("'%s':'constant',[%s]", term.getName(),
                    Op.str(t.getValue()));
        }
        if (term instanceof Cosine) {
            Cosine t = (Cosine) term;
            return String.format("'%s':'cosinemf',[%s]", term.getName(),
                    Op.join(" ", t.getCenter(), t.getWidth()));
        }
        if (term instanceof Discrete) {
            Discrete t = (Discrete) term;
            return String.format("'%s':'discretemf',[%s]", term.getName(),
                    Op.join(Discrete.toList(t.getXY()), " "));
        }

        if (term instanceof Function) {
            Function t = (Function) term;
            return String.format("'%s':'function',[%s]", term.getName(),
                    t.getFormula());
        }
        if (term instanceof Gaussian) {
            Gaussian t = (Gaussian) term;
            return String.format("'%s':'gaussmf',[%s]", term.getName(),
                    Op.join(" ", t.getStandardDeviation(), t.getMean()));
        }
        if (term instanceof GaussianProduct) {
            GaussianProduct t = (GaussianProduct) term;
            return String.format("'%s':'gauss2mf',[%s]", term.getName(),
                    Op.join(" ", t.getStandardDeviationA(), t.getMeanA(),
                            t.getStandardDeviationB(), t.getMeanB()));
        }
        if (term instanceof Linear) {
            Linear t = (Linear) term;
            return String.format("'%s':'linear',[%s]", term.getName(),
                    Op.join(t.getCoefficients(), " "));
        }
        if (term instanceof PiShape) {
            PiShape t = (PiShape) term;
            return String.format("'%s':'pimf',[%s]", term.getName(),
                    Op.join(" ", t.getBottomLeft(), t.getTopLeft(),
                            t.getTopRight(), t.getBottomRight()));
        }
        if (term instanceof Ramp) {
            Ramp t = (Ramp) term;
            return String.format("'%s':'rampmf',[%s]", term.getName(),
                    Op.join(" ", t.getStart(), t.getEnd()));
        }
        if (term instanceof Rectangle) {
            Rectangle t = (Rectangle) term;
            return String.format("'%s':'rectmf',[%s]", term.getName(),
                    Op.join(" ", t.getStart(), t.getEnd()));
        }
        if (term instanceof SigmoidDifference) {
            SigmoidDifference t = (SigmoidDifference) term;
            return String.format("'%s':'dsigmf',[%s]", term.getName(),
                    Op.join(" ", t.getRising(), t.getLeft(),
                            t.getFalling(), t.getRight()));
        }
        if (term instanceof Sigmoid) {
            Sigmoid t = (Sigmoid) term;
            return String.format("'%s':'sigmf',[%s]", term.getName(),
                    Op.join(" ", t.getSlope(), t.getInflection()));
        }
        if (term instanceof SigmoidProduct) {
            SigmoidProduct t = (SigmoidProduct) term;
            return String.format("'%s':'psigmf',[%s]", term.getName(),
                    Op.join(" ", t.getRising(), t.getLeft(),
                            t.getFalling(), t.getRight()));
        }
        if (term instanceof SShape) {
            SShape t = (SShape) term;
            return String.format("'%s':'smf',[%s]", term.getName(),
                    Op.join(" ", t.getStart(), t.getEnd()));
        }
        if (term instanceof Spike) {
            Spike t = (Spike) term;
            return String.format("'%s':'spikemf',[%s]", term.getName(),
                    Op.join(" ", t.getCenter(), t.getWidth()));
        }
        if (term instanceof Trapezoid) {
            Trapezoid t = (Trapezoid) term;
            return String.format("'%s':'trapmf',[%s]", term.getName(),
                    Op.join(" ", t.getVertexA(), t.getVertexB(), t.getVertexC(), t.getVertexD()));
        }
        if (term instanceof Triangle) {
            Triangle t = (Triangle) term;
            return String.format("'%s':'trimf',[%s]", term.getName(),
                    Op.join(" ", t.getVertexA(), t.getVertexB(), t.getVertexC()));
        }
        if (term instanceof ZShape) {
            ZShape t = (ZShape) term;
            return String.format("'%s':'zmf',[%s]", term.getName(),
                    Op.join(" ", t.getStart(), t.getEnd()));
        }
        throw new RuntimeException(String.format("[export error] "
                + "term of class <%s> not supported", term.getClass().getName()));
    }

    @Override
    public FisExporter clone() throws CloneNotSupportedException {
        return (FisExporter) super.clone();
    }

}
