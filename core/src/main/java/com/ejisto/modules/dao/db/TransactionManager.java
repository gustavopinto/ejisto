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

/**
* Created by IntelliJ IDEA.
* User: celestino
* Date: 3/8/13
* Time: 5:42 PM
*/
public enum TransactionManager {
    DEFAULT {
        @Override
        public void commit(EmbeddedDatabaseManager target) {
            target.internalCommit();
        }

        @Override
        public void rollback(EmbeddedDatabaseManager target) {
            target.internalRollback();
        }
    },
    NO_OP {
        @Override
        public void commit(EmbeddedDatabaseManager target) {
        }

        @Override
        public void rollback(EmbeddedDatabaseManager target) {
        }
    },
    READ_ONLY {
        @Override
        public void commit(EmbeddedDatabaseManager target) {
        }

        @Override
        public void rollback(EmbeddedDatabaseManager target) {
            target.internalRollback();
        }
    };
    public abstract void commit(EmbeddedDatabaseManager target);
    public abstract void rollback(EmbeddedDatabaseManager target);
}
