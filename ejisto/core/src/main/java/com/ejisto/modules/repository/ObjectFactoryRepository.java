/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.repository;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.ObjectFactoryDao;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.factory.impl.ArrayFactory;
import com.ejisto.util.ExternalizableService;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/11/10
 * Time: 4:41 PM
 */

public class ObjectFactoryRepository extends ExternalizableService<ObjectFactoryDao> implements InitializingBean {
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static final String DEFAULT = "java.lang.Object";
    private static final Pattern TYPE_EXTRACTOR = Pattern.compile("\\[?L?([a-zA-Z0-9\\.]+);?\\[?\\]?");
    private static final Pattern ARRAY_MATCHER = Pattern.compile("(\\[L([a-zA-Z0-9\\.]+);)|([a-zA-Z0-9\\.]+\\[\\])");
    private static final ObjectFactoryRepository INSTANCE = new ObjectFactoryRepository();
    private static final String ENUM_FACTORY_CLASS_NAME = "com.ejisto.modules.factory.impl.EnumFactory";
    private final Map<String, String> factories = new HashMap<String, String>();
    private final Map<String, String> primitives = new HashMap<String, String>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    enum ObjectType {
        ARRAY, ENUM, OBJECT;
    }


    @Resource private EventManager eventManager;
    @Resource private ObjectFactoryDao objectFactoryDao;

    public static ObjectFactoryRepository getInstance() {
        return INSTANCE;
    }

    private ObjectFactoryRepository() {
        registerObjectFactory("com.ejisto.modules.factory.impl.AtomicIntegerFactory",
                              "java.util.concurrent.atomic.AtomicInteger", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.AtomicLongFactory",
                              "java.util.concurrent.atomic.AtomicLong", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.BooleanFactory", "java.lang.Boolean", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.NumberFactory", "java.lang.Number", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.StringFactory", "java.lang.String", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.DefaultObjectFactory", DEFAULT, false);
        registerObjectFactory("com.ejisto.modules.factory.impl.CollectionFactory", "java.util.Collection", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.MapFactory", "java.util.Map", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.DateFactory", "java.util.Date", false);

        //populating primitive types map
        primitives.put("int", "java.lang.Integer");
        primitives.put("long", "java.lang.Long");
        primitives.put("char", "java.lang.Character");
        primitives.put("byte", "java.lang.Byte");
        primitives.put("boolean", "java.lang.Boolean");
        primitives.put("double", "java.lang.Double");
        primitives.put("float", "java.lang.Float");
        primitives.put("short", "java.lang.Short");

    }

    public void registerObjectFactory(String objectFactoryClassName, String targetClassName) {
        registerObjectFactory(objectFactoryClassName, targetClassName, true);
    }

    public void registerObjectFactory(String objectFactoryClassName, String targetClassName, boolean notify) {
        String message;
        boolean error = factories.containsKey(targetClassName);
        if (!error) {
            factories.put(targetClassName, objectFactoryClassName);
            insertObjectFactory(objectFactoryClassName, targetClassName);
            message = format("registered ObjectFactory [%s] for class %s", objectFactoryClassName, targetClassName);
        } else {
            message = format("rejected ObjectFactory [%s]", objectFactoryClassName);
        }
        if (notify && eventManager != null) {
            eventManager.publishEvent(new StatusBarMessage(this, message, error));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> getObjectFactory(String objectClassName, String contextPath) throws Exception {
        String objectFactoryClass = getObjectFactoryClass(objectClassName, contextPath);
        ObjectFactory<T> objectFactory = loadObjectFactory(objectFactoryClass);
        if (isArray(objectClassName)) {
            return (ObjectFactory<T>) new ArrayFactory<T>(objectFactory);
        }
        return objectFactory;
    }


    @SuppressWarnings("unchecked")
    private <T> ObjectFactory<T> loadObjectFactory(String className) {
        try {
            return (ObjectFactory<T>) Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String getObjectFactoryClass(String objectClassName, String contextPath) {
        try {
            syncObjectFactories();
            String className = getActualType(objectClassName);
            return scanForObjectFactory(retrieveClassPool(contextPath).get(className));
        } catch (Exception e) {
            logger.error("getObjectFactory failed with exception, returning default one", e);
            return factories.get(DEFAULT);
        }
    }

    boolean isArray(String type) {
        return ARRAY_MATCHER.matcher(type).matches();
    }

    String getActualType(String type) {
        String actualType = type;
        Matcher m = TYPE_EXTRACTOR.matcher(actualType);
        if (m.matches()) {
            actualType = m.group(1);
        }
        if (primitives.containsKey(actualType)) {
            return primitives.get(actualType);
        }
        return actualType;
    }

    private void insertObjectFactory(String objectFactoryClassName, String targetClassName) {
        if (initialized.get()) {
            objectFactoryDao.insert(new RegisteredObjectFactory(objectFactoryClassName, targetClassName));
        }
    }

    private synchronized void syncObjectFactories() {
        if (!initialized.get()) {
            checkDao();
            for (RegisteredObjectFactory registeredObjectFactory : objectFactoryDao.loadAll()) {
                factories.put(registeredObjectFactory.getTargetClassName(), registeredObjectFactory.getClassName());
            }
            initialized.compareAndSet(false, true);
        }
    }

    private ClassPool retrieveClassPool(String contextPath) {
        return ClassPoolRepository.getRegisteredClassPool(contextPath);
    }

    private String scanForObjectFactory(CtClass objectClass) throws Exception {
        try {
            trace(format("Searching for a factory for [%s]", objectClass.getName()));
            if (objectClass.isEnum()) {
                trace("target class is an Enum. Returning EnumFactory");
                return ENUM_FACTORY_CLASS_NAME;
            }
            if (factories.containsKey(objectClass.getName())) {
                trace("found!");
                return factories.get(objectClass.getName());
            }
            String factory;
            trace("not found. Trying with implemented interfaces...");
            for (CtClass c : objectClass.getInterfaces()) {
                factory = scanForObjectFactory(c);
                if (factory != null) {
                    return factory;
                }
            }
            if (!objectClass.isInterface()) {
                trace("not found. Trying with super class...");
                factory = scanForObjectFactory(objectClass.getSuperclass());
                if (factory != null) {
                    return factory;
                }
                return factories.get(DEFAULT);
            }
            return null;
        } finally {
            objectClass.detach();
        }
    }

    private void trace(String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }

    @Override
    protected ObjectFactoryDao getDaoInstance() {
        return objectFactoryDao;
    }

    @Override
    protected void setDaoInstance(ObjectFactoryDao daoInstance) {
        this.objectFactoryDao = daoInstance;
    }

    @Override
    protected Class<ObjectFactoryDao> getDaoClass() {
        return ObjectFactoryDao.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialized.compareAndSet(false, true);
    }
}
