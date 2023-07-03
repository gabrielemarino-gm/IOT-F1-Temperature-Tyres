package it.unipi.iot.model;

import it.unipi.iot.enumActuatorStatus.*;

// Questa classe rappresenta un attuatore
public class Actuator
{
    private int tyre_position;
    private String addr;
    private String resource;
    private boolean isActive; // Se è attivo (non Offline)
    private boolean isOn; // Se è acceso o spento (solo se Online)

    private String status;
    public Actuator(int pos, String addr, String r)
    {
        this.tyre_position = pos;
        this.addr = addr;
        this.resource = r;
        this.isActive = true;
        this.isOn = false;
        this.status = "UNDEFINED";
    }

    public int getTyre_position() {return tyre_position;}
    public String getAddr() {return addr;}
    public String getResource() {return resource;}
    public String getStatus() {return status;}
    public boolean isActive() {return isActive;}
    public boolean isOn() {return isOn;}

    public void setAddr(String addr) {this.addr = addr;}
    public void setResource(String r) {this.resource = r;}

    public void setTyre_position(int tyre_position) {this.tyre_position = tyre_position;}
    public void setStatus(String s) {status = s;}
    public void inactive(){isActive = false;}
    public void toggle(){isOn = !isOn;}
}
