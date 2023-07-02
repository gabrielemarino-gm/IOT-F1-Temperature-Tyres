package it.unipi.iot.model;

// Questa classe rappresenta un attuatore
public class Actuator {
    private int tyre_position;
    private String addr;
    private String resource;
    private boolean isActive; // Se e' attivo (non Offline)
    private boolean isOn; // Se e' acceso o spento (solo se Online)

    public Actuator(int pos, String addr, String r)
    {
        tyre_position = pos;
        this.addr = addr;
        this.resource = r;
        isActive = true;
        isOn = false;
    }

    public int getTyre_position() {return tyre_position;}
    public String getAddr() {return addr;}
    public String getResource() {return resource;}
    public boolean isActive() {return isActive;}
    public boolean isOn() {return isOn;}

    public void setAddr(String addr) {this.addr = addr;}
    public void setResource(String r) {this.resource = r;}

    public void setTyre_position(int tyre_position) {this.tyre_position = tyre_position;}
    public void inactive(){isActive = false;}
    public void toggle(){isOn = !isOn;}
}
