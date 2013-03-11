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

package com.ejisto.modules.dao.db;

import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.web.util.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/5/13
 * Time: 8:08 AM
 */
public class JSONCollectionSerializer<E, T extends Collection<E>> implements Serializer<T>, Serializable {

    @Override
    public void serialize(DataOutput dataOutput, T t) throws IOException {
        dataOutput.writeUTF(JSONUtil.encode(t));
    }

    @Override
    public T deserialize(DataInput dataInput, int i) throws IOException {
        return JSONUtil.decode(dataInput.readUTF(),
                               new TypeReference<T>() {
                               });
    }
}