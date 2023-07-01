package it.unipi.iot.model;

public class Actuator {
    private int tyre_position;
    private String addr;
    private boolean isActive;
    private boolean isOn;

    public Actuator(int pos, String addr){
        tyre_position = pos;
        this.addr = addr;
        isActive = true;
        isOn = false;
    }

    public int getTyre_position() {return tyre_position;}
    public String getAddr() {return addr;}
    public boolean isActive() {return isActive;}
    public boolean isOn() {return isOn;}

    public void setAddr(String addr) {this.addr = addr;}
    public void setTyre_position(int tyre_position) {this.tyre_position = tyre_position;}
    public void inactive(){isActive = false;}
    public void toggle(){isOn = !isOn;}
}
