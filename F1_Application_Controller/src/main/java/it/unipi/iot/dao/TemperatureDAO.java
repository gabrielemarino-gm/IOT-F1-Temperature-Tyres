package it.unipi.iot.dao;
import  it.unipi.iot.dao.exception.DAOException;
import  it.unipi.iot.model.Temperature;
import  it.unipi.iot.dao.base.BaseMySQLDAO;

import java.sql.*;


public class TemperatureDAO extends BaseMySQLDAO
{
    void writeTemperature(Temperature temperature, String nameTab, Object... params ) throws DAOException
    {
        StringBuilder insertTemperature = new StringBuilder();
        insertTemperature.append("insert into " + nameTab + " (temperature, timestamp, tyre_position) values");
        insertTemperature.append("(?,?,?)");

        Connection connectionParam = null;
        if (params != null && params.length > 0 && params[0] instanceof Connection)
        {
            connectionParam = (Connection)params[0];
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = connectionParam != null ? connectionParam : getConnection();
            preparedStatement = connection.prepareStatement(insertTemperature.toString(), Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setDouble(1, temperature.getTemperatureValue());
            preparedStatement.setTimestamp(2, new Timestamp(temperature.getTimestamp().getTime()));
            preparedStatement.setInt(3, temperature.getTyrePosition());
           
            preparedStatement.executeUpdate();

        }
        catch(Exception ex)
        {
            throw new DAOException(ex);
        }
    }
}
