/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.modules.factory.impl;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/4/12
 * Time: 6:35 PM
 */
public class BooleanFactory implements ObjectFactory<Boolean> {
    @Override
    public Class<?> getTargetClass() {
        return Boolean.class;
    }

    @Override
    public String getTargetClassName() {
        return "java.lang.Boolean";
    }

    @Override
    public Boolean create(MockedField m, Boolean actualValue) {
        return Boolean.valueOf(m.getFieldValue());
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public Boolean createRandomValue() {
        return null;
    }

}
