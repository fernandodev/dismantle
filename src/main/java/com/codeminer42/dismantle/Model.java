package com.codeminer42.dismantle;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class Model {
    abstract protected Map<String, String> externalRepresentationKeyPaths();

    protected Model() {

    }

    protected Model(Map<String, Object> externalRepresentation) {
        Map<String, String> selfRepresentation = this.externalRepresentationKeyPaths();
        for (String property : selfRepresentation.keySet()) {
            Object transformable = externalRepresentation.get(selfRepresentation.get(property));
            assignProperty(property, transformable);
        }
    }

    public Map<String, Object> externalRepresentation() {
        Map<String, String> selfRepresentation = this.externalRepresentationKeyPaths();
        Map<String, Object> representation = new HashMap<String, Object>();
        for (String property : selfRepresentation.keySet()) {
            representation.put(property, tryToGetField(property));
        }
        return representation;
    }

    private void assignProperty(String property, Object mapValue) {
        Object result = null;
        try {
            try {
                result = tryToInvokeTransformation(property, mapValue);
            } catch (NoSuchMethodException e) {
                // try to setTheField with the result we got
                result = mapValue;
            } catch(Exception e) {
                // Unexpected error while trying to invoke the transform method.
            } finally {
                this.tryToSetField(property, result);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private Object tryToInvokeTransformation(String property, Object mapValue) throws NoSuchMethodException {
        try {
            Method method = this.getClass().getMethod("transformTo" + property.substring(0, 1).toUpperCase() + property.substring(1), Object.class);
            return method.invoke(this, mapValue);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void tryToSetField(String property, Object data) {
        try {
            Field field = this.getClass().getDeclaredField(property);
            field.setAccessible(true);
            field.set(this, data);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object tryToGetField(String property) {
        try {
            Field field = this.getClass().getDeclaredField(property);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
}
