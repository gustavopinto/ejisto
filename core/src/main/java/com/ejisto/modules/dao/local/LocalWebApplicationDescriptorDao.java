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

import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalWebApplicationDescriptorDao extends BaseLocalDao {

    public LocalWebApplicationDescriptorDao(EmbeddedDatabaseManager database) {
        super(database);
    }

    public WebApplicationDescriptor load(String contextPath) {
        return loadAllDescriptors().get(contextPath);
    }

    public List<WebApplicationDescriptor> loadAll() {
        return new ArrayList<>(loadAllDescriptors().values());
    }

    public void insert(final WebApplicationDescriptor descriptor) {
        transactionalOperation(() -> {
            Map<String, WebApplicationDescriptor> descriptors = loadAllDescriptors();
            internalDelete(descriptor, descriptors);
            descriptors.put(descriptor.getContextPath(), WebApplicationDescriptor.copyOf(descriptor));
            return null;
        });
    }

    public void delete(final WebApplicationDescriptor descriptor) {
        transactionalOperation(() -> {
            internalDelete(descriptor, loadAllDescriptors());
            return null;
        });
    }

    private void internalDelete(WebApplicationDescriptor descriptor, Map<String, WebApplicationDescriptor> descriptors) {
        descriptors.remove(descriptor.getContextPath());
    }

    private Map<String, WebApplicationDescriptor> loadAllDescriptors() {
        return getDatabase().getWebApplicationDescriptors().orElseThrow(IllegalStateException::new);
    }
}
