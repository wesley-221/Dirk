package com.beneluwux.models.command;

public class CommandParameter {
    private String key;
    private Object value;
    private Boolean parsedCorrectly;

    public CommandParameter(String key, Object value, Boolean parsedCorrectly) {
        this.key = key;
        this.value = value;
        this.parsedCorrectly = parsedCorrectly;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Boolean isParsedCorrectly() {
        return parsedCorrectly;
    }

    public void setParsedCorrectly(Boolean parsedCorrectly) {
        this.parsedCorrectly = parsedCorrectly;
    }

    @Override
    public String toString() {
        return "CommandParameter{" +
                "parameterKey='" + key + '\'' +
                ", paramaterValue=" + value +
                ", parameterParsedCorrectly=" + parsedCorrectly +
                ", parameterType=" + value.getClass() +
                '}';
    }
}
