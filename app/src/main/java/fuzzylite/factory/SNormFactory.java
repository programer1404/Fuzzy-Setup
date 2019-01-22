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
package fuzzylite.factory;

import fuzzylite.norm.SNorm;
import fuzzylite.norm.s.*;

/**
 The SNormFactory class is a ConstructionFactory of SNorm%s.

 @author Juan Rada-Vilela, Ph.D.
 @see SNorm
 @see ConstructionFactory
 @see FactoryManager
 @since 4.0
 */
public class SNormFactory extends ConstructionFactory<SNorm> {

    public SNormFactory() {
        register("", null);
        register(AlgebraicSum.class);
        register(BoundedSum.class);
        register(DrasticSum.class);
        register(EinsteinSum.class);
        register(HamacherSum.class);
        register(Maximum.class);
        register(NilpotentMaximum.class);
        register(NormalizedSum.class);
        register(UnboundedSum.class);
    }

    @Override
    public SNormFactory clone() throws CloneNotSupportedException {
        return (SNormFactory) super.clone();
    }
}
