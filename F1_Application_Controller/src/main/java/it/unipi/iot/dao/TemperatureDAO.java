package it.unipi.iot.dao;

import  it.unipi.iot.dao.exception.DAOException;
import  it.unipi.iot.model.Temperature;
import  it.unipi.iot.dao.base.BaseMySQLDAO;

import java.sql.*;
import java.util.ArrayList;


public class TemperatureDAO extends BaseMySQLDAO
{
    public static void writeTemperature(Temperature temperature, String nameTab) throws DAOException
    {
        StringBuilder insertTemperature = new StringBuilder();
        insertTemperature.append("insert into " + nameTab + " (temperature, timestamp, tyre_position) values");
        insertTemperature.append("(?,?,?)");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(insertTemperature.toString(), Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setDouble(1, temperature.getTemperatureValue());
            preparedStatement.setTimestamp(2, new Timestamp(temperature.getTimestamp().getTime()));
            preparedStatement.setInt(3, temperature.getTyrePosition());
           
            preparedStatement.executeUpdate();
            connection.close();

        }
        catch(Exception ex)
        {
            throw new DAOException(ex);
        }
        finally {

            closePool();
        }
    }

    public static ArrayList<Temperature> getLastTemperature(String nameTab) throws DAOException
    {
        ArrayList<Temperature> toRet = new ArrayList<>();

        StringBuilder getTemperatures  = new StringBuilder();
        getTemperatures.append("select t1.tyre_position, t1.temperature " +
                "from " + nameTab + " as t1 inner join " +
                "( select tyre_position, max(timestamp) as max_timestamp " +
                "from " + nameTab + " " +
                "where timestamp >= now() - interval 5 minute " +
                "group by tyre_position) as t2 " +
                "on t1.tyre_position = t2.tyre_position and t1.timestamp = t2.max_timestamp;\n");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(getTemperatures.toString(), Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                Temperature newT = new Temperature();
                newT.setTemperatureValue(rs.getDouble("temperature"));
                newT.setTyrePosition(rs.getInt("tyre_position"));
                toRet.add(newT);
            }

            connection.close();
        }
        catch(Exception ex)
        {
            throw new DAOException(ex);
        }
        finally {
            closePool();
        }

        return toRet;
    }
}
