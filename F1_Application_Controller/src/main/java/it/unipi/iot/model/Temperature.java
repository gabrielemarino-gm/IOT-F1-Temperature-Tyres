package it.unipi.iot.model;

import javax.xml.crypto.Data;
import java.sql.Date;
import java.sql.Timestamp;

public class Temperature
{
    private long id;
    private double temperatureValue;
    private Date timestamp;
    private int tyrePosition;

    public Temperature(){}

    public void setTemperatureValue (double t) {this.temperatureValue = t;}
    public void setTimestamp (Date t) {this.timestamp = t;}
    public void setTyrePosition (int p) {this.tyrePosition = p;}

    public double getTemperatureValue () {return temperatureValue;}
    public Date getTimestamp () {return timestamp;}
    public int getTyrePosition () {return tyrePosition;}

    @Override
    public String toString() {
        return "Temperature{" +
                "id=" + id +
                ", temperatureValue=" + temperatureValue +
                ", timestamp=" + timestamp +
                ", tyrePosition=" + tyrePosition +
                '}';
    }
}
