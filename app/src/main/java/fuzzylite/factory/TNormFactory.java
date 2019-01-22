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

import fuzzylite.norm.TNorm;
import fuzzylite.norm.t.*;

/**
 The TNormFactory class is a ConstructionFactory of TNorm%s.

 @author Juan Rada-Vilela, Ph.D.
 @see TNorm
 @see ConstructionFactory
 @see FactoryManager
 @since 4.0
 */
public class TNormFactory extends ConstructionFactory<TNorm> {

    public TNormFactory() {
        register("", null);
        register(AlgebraicProduct.class);
        register(BoundedDifference.class);
        register(DrasticProduct.class);
        register(EinsteinProduct.class);
        register(HamacherProduct.class);
        register(Minimum.class);
        register(NilpotentMinimum.class);
    }

    @Override
    public TNormFactory clone() throws CloneNotSupportedException {
        return (TNormFactory) super.clone();
    }
}
