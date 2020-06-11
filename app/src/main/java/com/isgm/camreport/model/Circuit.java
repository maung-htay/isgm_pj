package com.isgm.camreport.model;

import androidx.annotation.NonNull;

public class Circuit {
    int circuitId;
    String circuit;

    public Circuit() { }

    public Circuit(int circuitId, String circuit) {
        this.circuitId = circuitId;
        this.circuit = circuit;
    }

    public int getId() {
        return circuitId;
    }

    public void setId(int id) {
        this.circuitId = id;
    }

    public String getCircuit() {
        return circuit;
    }

    public void setCircuit(String circuit) {
        this.circuit = circuit;
    }

    @NonNull
    @Override
    public String toString()
    {
        return this.circuit;
    }

}
