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

import fuzzylite.Op.Pair;
import fuzzylite.Engine;
import fuzzylite.Op;
import fuzzylite.activation.General;
import fuzzylite.defuzzifier.*;
import fuzzylite.factory.FactoryManager;
import fuzzylite.hedge.*;
import fuzzylite.norm.s.*;
import fuzzylite.norm.t.*;
import fuzzylite.rule.Rule;
import fuzzylite.rule.RuleBlock;
import fuzzylite.term.*;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;
import fuzzylite.variable.Variable;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 The FisImporter class is an Importer that configures an Engine and its
 components from utilizing the Fuzzy Inference System format for Matlab or
 Octave.

 @author Juan Rada-Vilela, Ph.D.
 @see FisExporter
 @see Importer
 @since 4.0
 */
public class FisImporter extends Importer {

    protected static final int AND = 0, OR = 1, IMP = 2, AGG = 3, DEFUZZ = 4, ALL = 5;

    public FisImporter() {

    }

    @Override
    public Engine fromString(String fis) {
        Engine engine = new Engine();

        BufferedReader fisReader = new BufferedReader(new StringReader(fis));
        String line;

        int lineNumber = 0;
        List<String> sections = new ArrayList<String>();
        try {
            while ((line = fisReader.readLine()) != null) {
                ++lineNumber;
                line = Op.split(line, "//", false).get(0);
                line = Op.split(line, "#", false).get(0);
                line = line.trim();
                // (%) indicates a comment only when used at the start of line
                if (line.isEmpty() || line.charAt(0) == '%') {
                    continue;
                }

                line = line.replaceAll(Pattern.quote("'"), "");

                if (line.startsWith("[System]")
                        || line.startsWith("[Input")
                        || line.startsWith("[Output")
                        || line.startsWith("[Rules]")) {
                    sections.add(line);
                } else if (!sections.isEmpty()) {
                    int lastIndex = sections.size() - 1;
                    String section = sections.get(lastIndex);
                    section += "\n" + line;
                    sections.set(lastIndex, section);
                } else {
                    throw new RuntimeException(String.format(
                            "[import error] line %d <%s> "
                            + "does not belong to any section", lineNumber, line));
                }
            }

            String[] configuration = new String[ALL];
            for (String section : sections) {
                if (section.startsWith("[System]")) {
                    importSystem(section, engine, configuration);
                } else if (section.startsWith("[Input")) {
                    importInput(section, engine);
                } else if (section.startsWith("[Output")) {
                    importOutput(section, engine);
                } else if (section.startsWith("[Rules]")) {
                    importRules(section, engine);
                } else {
                    throw new RuntimeException(String.format(
                            "[import error] section not recognized: %s", section));
                }
                engine.configure(translateTNorm(configuration[AND]), translateSNorm(configuration[OR]),
                        translateTNorm(configuration[IMP]), translateSNorm(configuration[AGG]),
                        translateDefuzzifier(configuration[DEFUZZ]), General.class.getSimpleName());
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return engine;
    }

    protected void importSystem(String section, Engine engine, String[] methods) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(section));
        reader.readLine(); //ignore first line [System]
        String line;
        while ((line = reader.readLine()) != null) {
            List<String> keyValue = Op.split(line, "=");
            String key = keyValue.get(0).trim();
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 1; i < keyValue.size(); ++i) {
                valueBuilder.append(keyValue.get(i));
            }
            String value = valueBuilder.toString().trim();

            if ("Name".equals(key)) {
                engine.setName(value);
            } else if ("AndMethod".equals(key)) {
                methods[AND] = value;
            } else if ("OrMethod".equals(key)) {
                methods[OR] = value;
            } else if ("ImpMethod".equals(key)) {
                methods[IMP] = value;
            } else if ("AggMethod".equals(key)) {
                methods[AGG] = value;
            } else if ("DefuzzMethod".equals(key)) {
                methods[DEFUZZ] = value;
            } else if ("Type".equals(key) || "Version".equals(key)
                    || "NumInputs".equals(key) || "NumOutputs".equals(key)
                    || "NumRules".equals(key) || "NumMFs".equals(key)) {
                //ignore because are redundant
            } else {
                throw new RuntimeException(String.format(
                        "[import error] token <%s> not recognized", key));
            }
        }
    }

    protected void importInput(String section, Engine engine) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(section));
        reader.readLine(); //ignore first line [InputX]

        InputVariable inputVariable = new InputVariable();
        engine.addInputVariable(inputVariable);

        String line;
        while ((line = reader.readLine()) != null) {
            List<String> keyValue = Op.split(line, "=");
            if (keyValue.size() != 2) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected a property of type "
                        + "'key=value', but found <%s>", line));
            }
            String key = keyValue.get(0).trim();
            String value = keyValue.get(1).trim();

            if ("Name".equals(key)) {
                inputVariable.setName(Op.validName(value));
            } else if ("Enabled".equals(key)) {
                inputVariable.setEnabled(Op.isEq(Op.toDouble(value), 1.0));
            } else if ("Range".equals(key)) {
                Pair<Double, Double> minmax = parseRange(value);
                inputVariable.setMinimum(minmax.getFirst());
                inputVariable.setMaximum(minmax.getSecond());
            } else if (key.startsWith("MF")) {
                inputVariable.addTerm(parseTerm(value, engine));
            } else if ("NumMFs".equals(key)) {
                //ignore
            } else {
                throw new RuntimeException(String.format(
                        "[import error] token <%s> not recognized", key));
            }
        }
    }

    protected void importOutput(String section, Engine engine) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(section));
        reader.readLine(); //ignore first line [InputX]

        OutputVariable outputVariable = new OutputVariable();
        engine.addOutputVariable(outputVariable);

        String line;
        while ((line = reader.readLine()) != null) {
            List<String> keyValue = Op.split(line, "=");
            if (keyValue.size() != 2) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected a property of type "
                        + "'key=value', but found <%s>", line));
            }
            String key = keyValue.get(0).trim();
            String value = keyValue.get(1).trim();

            if ("Name".equals(key)) {
                outputVariable.setName(Op.validName(value));
            } else if ("Enabled".equals(key)) {
                outputVariable.setEnabled(Op.isEq(Op.toDouble(value), 1.0));
            } else if ("Range".equals(key)) {
                Pair<Double, Double> minmax = parseRange(value);
                outputVariable.setMinimum(minmax.getFirst());
                outputVariable.setMaximum(minmax.getSecond());
            } else if (key.startsWith("MF")) {
                outputVariable.addTerm(parseTerm(value, engine));
            } else if ("Default".equals(key)) {
                outputVariable.setDefaultValue(Op.toDouble(value));
            } else if ("LockPrevious".equals(key)) {
                outputVariable.setLockPreviousValue(Op.isEq(Op.toDouble(value), 1.0));
            } else if ("LockRange".equals(key)) {
                outputVariable.setLockValueInRange(Op.isEq(Op.toDouble(value), 1.0));
            } else if ("NumMFs".equals(key)) {
                //ignore
            } else {
                throw new RuntimeException(String.format(
                        "[import error] token <%s> not recognized", key));
            }
        }
    }

    protected void importRules(String section, Engine engine) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(section));
        reader.readLine(); //ignore first line [Rules]

        RuleBlock ruleBlock = new RuleBlock();
        engine.addRuleBlock(ruleBlock);

        String line;
        while ((line = reader.readLine()) != null) {
            List<String> inputsAndRest = Op.split(line, ",");
            if (inputsAndRest.size() != 2) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected rule to match pattern "
                        + "<'i '+, 'o '+ (w) : '1|2'>, but found instead <%s>", line));
            }

            List<String> outputsAndRest = Op.split(inputsAndRest.get(1), ":");
            if (outputsAndRest.size() != 2) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected rule to match pattern "
                        + "<'i '+, 'o '+ (w) : '1|2'>, but found instead <%s>", line));
            }
            List<String> inputs = Op.split(inputsAndRest.get(0).trim(), " ");
            List<String> outputs = Op.split(outputsAndRest.get(0).trim(), " ");
            String weightInParenthesis = outputs.get(outputs.size() - 1);
            outputs.remove(outputs.size() - 1);
            String connector = outputsAndRest.get(1).trim();

            if (inputs.size() != engine.numberOfInputVariables()) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected <%d> input variables, "
                        + "but found <%d> input variables in rule <%s>",
                        engine.numberOfInputVariables(),
                        inputs.size(), line));
            }
            if (outputs.size() != engine.numberOfOutputVariables()) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected <%d> output variables, "
                        + "but found <%d> output variables in rule <%s>",
                        engine.numberOfOutputVariables(),
                        outputs.size(), line));
            }

            List<String> antecedent = new ArrayList<String>();
            List<String> consequent = new ArrayList<String>();

            for (int i = 0; i < inputs.size(); ++i) {
                double inputCode = Op.toDouble(inputs.get(i));
                if (Op.isEq(inputCode, 0.0)) {
                    continue;
                }
                InputVariable inputVariable = engine.getInputVariable(i);
                String proposition = String.format("%s %s %s",
                        inputVariable.getName(), Rule.FL_IS,
                        translateProposition(inputCode, inputVariable));
                antecedent.add(proposition);
            }

            for (int i = 0; i < outputs.size(); ++i) {
                double outputCode = Op.toDouble(outputs.get(i));
                if (Op.isEq(outputCode, 0.0)) {
                    continue;
                }
                OutputVariable outputVariable = engine.getOutputVariable(i);
                String proposition = String.format("%s %s %s",
                        outputVariable.getName(), Rule.FL_IS,
                        translateProposition(outputCode, outputVariable));
                consequent.add(proposition);
            }

            StringBuilder ruleText = new StringBuilder();
            ruleText.append(Rule.FL_IF).append(" ");
            for (Iterator<String> it = antecedent.iterator(); it.hasNext();) {
                ruleText.append(it.next());
                if (it.hasNext()) {
                    ruleText.append(" ");
                    if ("1".equals(connector)) {
                        ruleText.append(Rule.FL_AND).append(" ");
                    } else if ("2".equals(connector)) {
                        ruleText.append(Rule.FL_OR).append(" ");
                    } else {
                        throw new RuntimeException(String.format(
                                "[syntax error] connector <%s> not recognized",
                                connector));
                    }
                }
            }

            ruleText.append(String.format(" %s ", Rule.FL_THEN));
            for (Iterator<String> it = consequent.iterator(); it.hasNext();) {
                ruleText.append(it.next());
                if (it.hasNext()) {
                    ruleText.append(String.format(" %s ", Rule.FL_AND));
                }
            }
            StringBuilder weightBuilder = new StringBuilder();
            for (char c : weightInParenthesis.toCharArray()) {
                if (c == '(' || c == ')' || c == ' ') {
                    continue;
                }
                weightBuilder.append(c);
            }
            String weightString = weightBuilder.toString();
            double weight = Op.toDouble(weightString);
            if (!Op.isEq(weight, 1.0)) {
                ruleText.append(String.format(" %s %s",
                        Rule.FL_WITH, Op.str(weight)));
            }
            Rule rule = new Rule(ruleText.toString());
            try {
                rule.load(engine);
            } finally {
                ruleBlock.addRule(rule);
            }
        }
    }

    protected String translateProposition(double code, Variable variable) {
        int intPart = (int) Math.floor(Math.abs(code)) - 1;
        double fracPart = Math.abs(code) % 1;

        if (intPart > variable.numberOfTerms()) {
            throw new RuntimeException(String.format(
                    "[syntax error] the code <%s> refers to a term out of range "
                    + "from variable <%s>", Op.str(code), variable.getName()));
        }

        boolean isAny = intPart < 0;
        StringBuilder result = new StringBuilder();
        if (code < 0) {
            result.append(new Not().getName()).append(" ");
        }
        if (Op.isEq(fracPart, 0.01)) {
            result.append(new Seldom().getName()).append(" ");
        } else if (Op.isEq(fracPart, 0.05)) {
            result.append(new Somewhat().getName()).append(" ");
        } else if (Op.isEq(fracPart, 0.2)) {
            result.append(new Very().getName()).append(" ");
        } else if (Op.isEq(fracPart, 0.3)) {
            result.append(new Extremely().getName()).append(" ");
        } else if (Op.isEq(fracPart, 0.4)) {
            result.append(new Very().getName()).append(" ");
            result.append(new Very().getName()).append(" ");
        } else if (Op.isEq(fracPart, 0.99)) {
            result.append(new Any().getName()).append(" ");
        } else if (!Op.isEq(fracPart, 0)) {
            throw new RuntimeException(String.format(
                    "[syntax error] no hedge defined in FIS format for <%s>",
                    Op.str(fracPart)));
        }
        if (!isAny) {
            result.append(variable.getTerm(intPart).getName());
        }
        return result.toString();
    }

    protected String translateTNorm(String name) {
        if ("min".equals(name)) {
            return Minimum.class.getSimpleName();
        }
        if ("prod".equals(name)) {
            return AlgebraicProduct.class.getSimpleName();
        }
        if ("bounded_difference".equals(name)) {
            return BoundedDifference.class.getSimpleName();
        }
        if ("drastic_product".equals(name)) {
            return DrasticProduct.class.getSimpleName();
        }
        if ("einstein_product".equals(name)) {
            return EinsteinProduct.class.getSimpleName();
        }
        if ("hamacher_product".equals(name)) {
            return HamacherProduct.class.getSimpleName();
        }
        if ("nilpotent_minimum".equals(name)) {
            return NilpotentMinimum.class.getSimpleName();
        }
        return name;
    }

    protected String translateSNorm(String name) {
        if ("max".equals(name)) {
            return Maximum.class.getSimpleName();
        }
        if ("probor".equals(name)) {
            return AlgebraicSum.class.getSimpleName();
        }
        if ("bounded_sum".equals(name)) {
            return BoundedSum.class.getSimpleName();
        }
        if ("normalized_sum".equals(name)) {
            return NormalizedSum.class.getSimpleName();
        }
        if ("drastic_sum".equals(name)) {
            return DrasticSum.class.getSimpleName();
        }
        if ("einstein_sum".equals(name)) {
            return EinsteinSum.class.getSimpleName();
        }
        if ("hamacher_sum".equals(name)) {
            return HamacherSum.class.getSimpleName();
        }
        if ("nilpotent_maximum".equals(name)) {
            return NilpotentMaximum.class.getSimpleName();
        }
        if ("sum".equals(name)) {
            return UnboundedSum.class.getSimpleName();
        }
        return name;
    }

    protected String translateDefuzzifier(String name) {
        if ("centroid".equals(name)) {
            return Centroid.class.getSimpleName();
        }
        if ("bisector".equals(name)) {
            return Bisector.class.getSimpleName();
        }
        if ("lom".equals(name)) {
            return LargestOfMaximum.class.getSimpleName();
        }
        if ("mom".equals(name)) {
            return MeanOfMaximum.class.getSimpleName();
        }
        if ("som".equals(name)) {
            return SmallestOfMaximum.class.getSimpleName();
        }
        if ("wtaver".equals(name)) {
            return WeightedAverage.class.getSimpleName();
        }
        if ("wtsum".equals(name)) {
            return WeightedSum.class.getSimpleName();
        }
        return name;
    }

    protected Pair<Double, Double> parseRange(String range) {
        List<String> minmax = Op.split(range, " ");
        if (minmax.size() != 2) {
            throw new RuntimeException(String.format(
                    "[syntax error] expected range in format '[begin end]', "
                    + "but found <%s>", range));
        }
        String begin = minmax.get(0);
        String end = minmax.get(1);
        if (begin.charAt(0) != '[' || end.charAt(end.length() - 1) != ']') {
            throw new RuntimeException(String.format(
                    "[syntax error] expected range in format '[begin end]', "
                    + "but found <%s>", range));
        }
        Pair<Double, Double> result = new Pair<Double, Double>();
        result.setFirst(Op.toDouble(begin.substring(1)));
        result.setSecond(Op.toDouble(end.substring(0, end.length() - 1)));
        return result;
    }

    protected Term parseTerm(String fis, Engine engine) {
        StringBuilder lineBuilder = new StringBuilder();
        for (char c : fis.toCharArray()) {
            if (!(c == '[' || c == ']')) {
                lineBuilder.append(c);
            }
        }
        String line = lineBuilder.toString();

        List<String> nameTerm = Op.split(line, ":");
        if (nameTerm.size() != 2) {
            throw new RuntimeException(String.format(
                    "[syntax error] expected term in format 'name':'class',[params], "
                    + "but found <%s>", line));
        }

        List<String> termParams = Op.split(nameTerm.get(1), ",");
        if (termParams.size() != 2) {
            throw new RuntimeException(String.format(
                    "[syntax error] expected term in format 'name':'class',[params], "
                    + "but found <%s>", line));
        }

        List<String> parameters = Op.split(termParams.get(1), " ");
        for (int i = 0; i < parameters.size(); ++i) {
            parameters.set(i, parameters.get(i).trim());
        }

        return createInstance(
                termParams.get(0).trim(),
                nameTerm.get(0).trim(),
                parameters, engine);
    }

    protected Term createInstance(String mClass, String name, List<String> parameters, Engine engine) {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("gbellmf", Bell.class.getSimpleName());
        mapping.put("binarymf", Binary.class.getSimpleName());
        mapping.put("concavemf", Concave.class.getSimpleName());
        mapping.put("constant", Constant.class.getSimpleName());
        mapping.put("cosinemf", Cosine.class.getSimpleName());
        mapping.put("function", Function.class.getSimpleName());
        mapping.put("discretemf", Discrete.class.getSimpleName());
        mapping.put("gaussmf", Gaussian.class.getSimpleName());
        mapping.put("gauss2mf", GaussianProduct.class.getSimpleName());
        mapping.put("linear", Linear.class.getSimpleName());
        mapping.put("pimf", PiShape.class.getSimpleName());
        mapping.put("rampmf", Ramp.class.getSimpleName());
        mapping.put("rectmf", Rectangle.class.getSimpleName());
        mapping.put("smf", SShape.class.getSimpleName());
        mapping.put("sigmf", Sigmoid.class.getSimpleName());
        mapping.put("dsigmf", SigmoidDifference.class.getSimpleName());
        mapping.put("psigmf", SigmoidProduct.class.getSimpleName());
        mapping.put("spikemf", Spike.class.getSimpleName());
        mapping.put("trapmf", Trapezoid.class.getSimpleName());
        mapping.put("trimf", Triangle.class.getSimpleName());
        mapping.put("zmf", ZShape.class.getSimpleName());

        List<String> sortedParameters = new ArrayList<String>(parameters);

        if ("gbellmf".equals(mClass) && parameters.size() >= 3) {
            sortedParameters.set(0, parameters.get(2));
            sortedParameters.set(1, parameters.get(0));
            sortedParameters.set(2, parameters.get(1));
        } else if ("gaussmf".equals(mClass) && parameters.size() >= 2) {
            sortedParameters.set(0, parameters.get(1));
            sortedParameters.set(1, parameters.get(0));
        } else if ("gauss2mf".equals(mClass) && parameters.size() >= 4) {
            sortedParameters.set(0, parameters.get(1));
            sortedParameters.set(1, parameters.get(0));
            sortedParameters.set(2, parameters.get(3));
            sortedParameters.set(3, parameters.get(2));
        } else if ("sigmf".equals(mClass) && parameters.size() >= 2) {
            sortedParameters.set(0, parameters.get(1));
            sortedParameters.set(1, parameters.get(0));
        } else if ("dsigmf".equals(mClass) && parameters.size() >= 4) {
            sortedParameters.set(0, parameters.get(1));
            sortedParameters.set(1, parameters.get(0));
            sortedParameters.set(2, parameters.get(2));
            sortedParameters.set(3, parameters.get(3));
        } else if ("psigmf".equals(mClass) && parameters.size() >= 4) {
            sortedParameters.set(0, parameters.get(1));
            sortedParameters.set(1, parameters.get(0));
            sortedParameters.set(2, parameters.get(2));
            sortedParameters.set(3, parameters.get(3));
        }

        String flClass = mapping.get(mClass);
        if (flClass == null) {
            flClass = mClass;
        }

        Term term = FactoryManager.instance().term().constructObject(flClass);
        term.updateReference(engine);
        term.setName(Op.validName(name));
        String separator = " ";
        if (term instanceof Function) {
            separator = "";
        }
        term.configure(Op.join(sortedParameters, separator));
        return term;
    }

    @Override
    public FisImporter clone() throws CloneNotSupportedException {
        return (FisImporter) super.clone();
    }

}
