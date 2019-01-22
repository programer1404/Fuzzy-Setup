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
package fuzzylite.rule;

import fuzzylite.Engine;
import fuzzylite.FuzzyLite;
import fuzzylite.Op;
import fuzzylite.factory.FactoryManager;
import fuzzylite.factory.HedgeFactory;
import fuzzylite.hedge.Any;
import fuzzylite.hedge.Hedge;
import fuzzylite.norm.SNorm;
import fuzzylite.norm.TNorm;
import fuzzylite.term.Function;
import fuzzylite.variable.OutputVariable;
import fuzzylite.variable.Variable;

import java.util.*;
import java.util.logging.Level;

/**
 The Antecedent class is an expression tree that represents and evaluates the
 antecedent of a Rule. The structure of a rule is: `if (antecedent) then
 (consequent)`. The structure of the antecedent of a rule is:

 `if variable is [hedge]* term [(and|or) variable is [hedge]* term]*`

 where `*`-marked elements may appear zero or more times, elements in brackets
 are optional, and elements in parentheses are compulsory.

 @author Juan Rada-Vilela, Ph.D.
 @see Consequent
 @see Rule
 @since 4.0
 */
public class Antecedent {

    private String text;
    private Expression expression;

    public Antecedent() {
        this.text = "";
        this.expression = null;
    }

    /**
     Gets the antecedent in text

     @return the antecedent in text
     */
    public String getText() {
        return text;
    }

    /**
     Sets the antecedent in text

     @param text is the antecedent in text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     Gets the expression tree of the antecedent

     @return the expression tree of the antecedent
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     Sets the expression tree of the antecedent

     @param expression is the expression tree of the antecedent
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     Indicates whether the antecedent is loaded

     @return whether the antecedent is loaded
     */
    public boolean isLoaded() {
        return expression != null;
    }

    /**
     Computes the activation degree of the antecedent on the expression tree
     from the root node

     @param conjunction is the conjunction operator from the RuleBlock
     @param disjunction is the disjunction operator from the RuleBlock
     @return the activation degree of the antecedent
     */
    public double activationDegree(TNorm conjunction, SNorm disjunction) {
        return this.activationDegree(conjunction, disjunction, expression);
    }

    /**
     Computes the activation degree of the antecedent on the expression tree
     from the given node

     @param conjunction is the conjunction operator from the RuleBlock
     @param disjunction is the disjunction operator from the RuleBlock
     @param node is a node in the expression tree of the antecedent
     @return the activation degree of the antecedent
     */
    public double activationDegree(TNorm conjunction, SNorm disjunction, Expression node) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[antecedent error] antecedent <%s> is not loaded", text));
        }
        final Expression.Type expressionType = node.type();
        if (expressionType == Expression.Type.Proposition) {
            Proposition proposition = (Proposition) node;
            if (!proposition.getVariable().isEnabled()) {
                return 0.0;
            }
            if (!proposition.getHedges().isEmpty()) {
                final int lastIndex = proposition.getHedges().size();
                ListIterator<Hedge> rit = proposition.getHedges().listIterator(lastIndex);
                Hedge any = rit.previous();
                //if last hedge is "Any", apply hedges in reverse order and return degree
                if (any instanceof Any) {
                    double result = any.hedge(Double.NaN);
                    while (rit.hasPrevious()) {
                        result = rit.previous().hedge(result);
                    }
                    return result;
                }
            }

            Variable variable = proposition.getVariable();
            double result = Double.NaN;
            Variable.Type variableType = variable.type();
            if (variableType == Variable.Type.Input) {
                result = proposition.getTerm().membership(variable.getValue());
            } else if (variableType == Variable.Type.Output) {
                result = ((OutputVariable) variable).fuzzyOutput().activationDegree(proposition.getTerm());
            }
            int lastIndex = proposition.getHedges().size();
            ListIterator<Hedge> reverseIterator = proposition.getHedges().listIterator(lastIndex);
            while (reverseIterator.hasPrevious()) {
                result = reverseIterator.previous().hedge(result);
            }
            return result;
        }

        if (expressionType == Expression.Type.Operator) {
            Operator operator = (Operator) node;
            if (operator.getLeft() == null || operator.getRight() == null) {
                throw new RuntimeException("[syntax error] left and right operators cannot be null");
            }
            if (Rule.FL_AND.equals(operator.getName())) {
                if (conjunction == null) {
                    throw new RuntimeException(String.format("[conjunction error] "
                            + "the following rule requires a conjunction operator:\n%s", text));
                }
                return conjunction.compute(
                        activationDegree(conjunction, disjunction, operator.getLeft()),
                        activationDegree(conjunction, disjunction, operator.getRight()));
            }
            if (Rule.FL_OR.equals(operator.getName())) {
                if (disjunction == null) {
                    throw new RuntimeException(String.format("[disjunction error] "
                            + "the following rule requires a disjunction operator:\n%s", text));
                }
                return disjunction.compute(
                        activationDegree(conjunction, disjunction, operator.getLeft()),
                        activationDegree(conjunction, disjunction, operator.getRight()));
            }
            throw new RuntimeException(String.format(
                    "[syntax error] operator <%s> not recognized",
                    operator.getName()));
        } else {
            throw new RuntimeException("[expression error] unknown instance of Expression");
        }
    }

    /**
     Unloads the antecedent
     */
    public void unload() {
        setExpression(null);
    }

    /**
     Loads the antecedent with the text obtained from Antecedent::getText() and
     uses the engine to identify and retrieve references to the input variables
     and output variables as required

     @param engine is the engine from which the rules are part of
     */
    public void load(Engine engine) {
        load(getText(), engine);
    }

    /**
     Loads the antecedent with the given text and uses the engine to identify
     and retrieve references to the input variables and output variables as
     required

     @param antecedent is the antecedent of the rule in text
     @param engine is the engine from which the rules are part of
     */
    public void load(String antecedent, Engine engine) {
        FuzzyLite.logger().log(Level.FINE, "Antecedent: {0}", antecedent);
        unload();
        setText(antecedent);
        if (antecedent.trim().isEmpty()) {
            throw new RuntimeException("[syntax error] antecedent is empty");
        }
        /*
         Builds an proposition tree from the antecedent of a fuzzy rule.
         The rules are:
         1) After a variable comes 'is',
         2) After 'is' comes a hedge or a term
         3) After a hedge comes a hedge or a term
         4) After a term comes a variable or an operator
         */

        Function function = new Function();
        String postfix = function.toPostfix(antecedent);
        FuzzyLite.logger().log(Level.FINE, "Postfix {0}", postfix);

        final byte S_VARIABLE = 1, S_IS = 2, S_HEDGE = 4, S_TERM = 8, S_AND_OR = 16;
        byte state = S_VARIABLE;
        Deque<Expression> expressionStack = new ArrayDeque<Expression>();
        Proposition proposition = null;

        StringTokenizer tokenizer = new StringTokenizer(postfix);
        String token = "";

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if ((state & S_VARIABLE) != 0) {
                Variable variable = null;
                if (engine.hasInputVariable(token)) {
                    variable = engine.getInputVariable(token);
                } else if (engine.hasOutputVariable(token)) {
                    variable = engine.getOutputVariable(token);
                }
                if (variable != null) {
                    proposition = new Proposition();
                    proposition.setVariable(variable);
                    expressionStack.push(proposition);

                    state = S_IS;
                    FuzzyLite.logger().log(Level.FINE, "Token <{0}> is variable", token);
                    continue;
                }
            }

            if ((state & S_IS) != 0) {
                if (Rule.FL_IS.equals(token)) {
                    state = S_HEDGE | S_TERM;
                    FuzzyLite.logger().log(Level.FINE, "Token <{0}> is keyword", token);
                    continue;
                }
            }

            if ((state & S_HEDGE) != 0) {
                HedgeFactory hedgeFactory = FactoryManager.instance().hedge();
                if (hedgeFactory.hasConstructor(token)) {
                    Hedge hedge = hedgeFactory.constructObject(token);
                    proposition.getHedges().add(hedge);
                    if (hedge instanceof Any) {
                        state = S_VARIABLE | S_AND_OR;
                    } else {
                        state = S_HEDGE | S_TERM;
                    }
                    FuzzyLite.logger().log(Level.FINE, "Token <{0}> is hedge", token);
                    continue;
                }
            }

            if ((state & S_TERM) != 0) {
                if (proposition.getVariable().hasTerm(token)) {
                    proposition.setTerm(proposition.getVariable().getTerm(token));
                    state = S_VARIABLE | S_AND_OR;
                    FuzzyLite.logger().log(Level.FINE, "Token <{0}> is term", token);
                    continue;
                }
            }

            if ((state & S_AND_OR) != 0) {
                if (Rule.FL_AND.equals(token) || Rule.FL_OR.equals(token)) {
                    if (expressionStack.size() < 2) {
                        throw new RuntimeException(String.format(
                                "[syntax error] logical operator <%s> expects at least two operands, but found <%d>",
                                token, expressionStack.size()));
                    }
                    Operator operator = new Operator();
                    operator.setName(token);
                    operator.setRight(expressionStack.pop());
                    operator.setLeft(expressionStack.pop());
                    expressionStack.push(operator);

                    state = S_VARIABLE | S_AND_OR;
                    FuzzyLite.logger().log(Level.FINE, "Subtree: ({0}) ({1})",
                            new Object[]{operator.getLeft(), operator.getRight()});
                    continue;
                }
            }

            //If reached this point, there was an error
            if ((state & S_VARIABLE) != 0 || (state & S_AND_OR) != 0) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected variable or logical operator, but found <%s>",
                        token));
            }
            if ((state & S_IS) != 0) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected keyword <%s>, but found <%s>",
                        Rule.FL_IS, token));
            }
            if ((state & S_HEDGE) != 0 || (state & S_TERM) != 0) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected hedge or term, but found <%s>",
                        token));
            }
            throw new RuntimeException(String.format(
                    "[syntax error] unexpected token <%s>",
                    token));
        }

        if (!((state & S_VARIABLE) != 0 || (state & S_AND_OR) != 0)) { //only acceptable final state
            if ((state & S_IS) != 0) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected keyword <%s> after <%s>",
                        Rule.FL_IS, token));
            }
            if ((state & S_HEDGE) != 0 || (state & S_TERM) != 0) {
                throw new RuntimeException(String.format(
                        "[syntax error] expected hedge or term, but found <%s>",
                        token));
            }
        }

        if (expressionStack.size() != 1) {
            List<String> errors = new LinkedList<String>();
            while (expressionStack.size() > 1) {
                Expression element = expressionStack.pop();
                errors.add(element.toString());
            }
            throw new RuntimeException(String.format(
                    "[syntax error] unable to parse the following expressions: <%s>",
                    Op.join(errors, " ")));
        }
        setExpression(expressionStack.pop());
    }

    /**
     Returns a string representation of the expression tree in infix notation

     @return a string representation of the expression tree in infix notation
     */
    @Override
    public String toString() {
        return this.toInfix(this.expression);
    }

    /**
     Returns a string representation of the expression tree utilizing prefix
     notation

     @return a string representation of the given expression tree utilizing prefix
     notation
     */
    public String toPrefix() {
        return this.toPrefix(this.expression);
    }

    public String toInfix() {
        return this.toInfix(this.expression);
    }

    public String toPostfix() {
        return this.toPostfix(this.expression);
    }

    public String toPrefix(Expression node) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[antecedent error] antecedent <%s> is not loaded",
                    this.text));
        }
        if (node instanceof Proposition) {
            return node.toString();
        }
        if (node instanceof Operator) {
            Operator operator = (Operator) node;
            return operator.toString() + " "
                    + this.toPrefix(operator.getLeft()) + " "
                    + this.toPrefix(operator.getRight()) + " ";
        }
        throw new RuntimeException(String.format(
                "[expression error] unexpected class <%s>",
                node.getClass().getSimpleName()));
    }

    public String toInfix(Expression node) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[antecedent error] antecedent <%s> is not loaded",
                    this.text));
        }
        if (node instanceof Proposition) {
            return node.toString();
        }
        if (node instanceof Operator) {
            Operator operator = (Operator) node;
            return this.toInfix(operator.getLeft()) + " "
                    + operator.toString() + " "
                    + this.toInfix(operator.getRight()) + " ";
        }
        throw new RuntimeException(String.format(
                "[expression error] unexpected class <%s>",
                node.getClass().getSimpleName()));
    }

    public String toPostfix(Expression node) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[antecedent error] antecedent <%s> is not loaded",
                    this.text));
        }
        if (node instanceof Proposition) {
            return node.toString();
        }
        if (node instanceof Operator) {
            Operator operator = (Operator) node;
            return this.toPostfix(operator.getLeft()) + " "
                    + this.toPostfix(operator.getRight()) + " "
                    + operator.toString() + " ";
        }
        throw new RuntimeException(String.format(
                "[expression error] unexpected class <%s>",
                node.getClass().getSimpleName()));
    }
}
