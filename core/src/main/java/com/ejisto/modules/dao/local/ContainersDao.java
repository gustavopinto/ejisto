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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.exception.UniqueConstraintViolated;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:54 AM
 */
public class ContainersDao extends BaseLocalDao {

    public List<Container> loadAll() {
        return new ArrayList<>(getDatabase().getContainers().values());
    }

    public Container load(String id) {
        return getDatabase().getContainers().get(id);
    }

    public boolean insert(Container container) {
        if(load(container.getId()) != null) {
            throw new UniqueConstraintViolated("Container.id cannot be '"+container.getId()+"'");
        }
        getDatabase().getContainers().put(container.getKey(), container);
        tryToCommit();
        return true;
    }

    public boolean delete(Container container) {
        getDatabase().getContainers().remove(container.getKey());
        tryToCommit();
        return true;
    }

}