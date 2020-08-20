package com.beneluwux.models.command;

public class CommandParameter {
    private String parameterKey;
    private Object paramaterValue;
    private Boolean parameterParsedCorrectly;

    public CommandParameter(String parameterKey, Object paramaterValue, Boolean parameterParsedCorrectly) {
        this.parameterKey = parameterKey;
        this.paramaterValue = paramaterValue;
        this.parameterParsedCorrectly = parameterParsedCorrectly;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public Object getParamaterValue() {
        return paramaterValue;
    }

    public void setParamaterValue(Object paramaterValue) {
        this.paramaterValue = paramaterValue;
    }

    public Boolean getParameterParsedCorrectly() {
        return parameterParsedCorrectly;
    }

    public void setParameterParsedCorrectly(Boolean parameterParsedCorrectly) {
        this.parameterParsedCorrectly = parameterParsedCorrectly;
    }

    @Override
    public String toString() {
        return "CommandParameter{" +
                "parameterKey='" + parameterKey + '\'' +
                ", paramaterValue=" + paramaterValue +
                ", parameterParsedCorrectly=" + parameterParsedCorrectly +
                ", parameterType=" + paramaterValue.getClass() +
                '}';
    }
}
