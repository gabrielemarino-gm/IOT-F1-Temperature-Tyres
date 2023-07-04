package it.unipi.iot.dao;

import  it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Actuator;
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

//    Funziona
    public static ArrayList<Actuator> getActiveActuators()
    {
        ArrayList<Actuator> toRet = new ArrayList<>();

        StringBuilder getActuator  = new StringBuilder();
        getActuator.append("select tyre_position, ipv6_addr, type " +
                "from actuators " +
                "where timestamp >= now() - interval 5 minute " +
                "order by timestamp desc\n");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(getActuator.toString(), Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = preparedStatement.executeQuery();

            if(rs.next())
            {
                Actuator toSet = new Actuator(rs.getInt("tyre_position"), rs.getString("ipv6_addr"), rs.getString("type"));
                toRet.add(toSet);
            }

            connection.close();
        }
        catch(Exception ex)
        {
//          throw new DAOException(ex);
            ex.printStackTrace();
        }
        finally
        {
            closePool();
        }

//      Se il DB non ha nessun attuatore registrato il metodo ritorna una lista con un solo attuatore con indirizzo ZERO.
        if (toRet.isEmpty())
            toRet.add(new Actuator(0, "0", "0"));
        System.out.println("DGB     DriverDB: " + toRet.toString());
        return toRet;
    }

    public static Actuator getActuator(int i, String s)
    {
        Actuator toRet = null;

        StringBuilder getActuator  = new StringBuilder();
        getActuator.append("select tyre_position, ipv6_addr, type " +
                            "from actuators " +
                            "where tyre_position=? and type=? and timestamp >= (now() - interval 5 minute) " +
                            "order by timestamp desc " +
                            "limit 1\n");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(getActuator.toString(), Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setInt(1, i);
            preparedStatement.setString(2, s);


            ResultSet rs = preparedStatement.executeQuery();

            if(rs.next()){
                toRet = new Actuator(rs.getInt("tyre_position"), rs.getString("ipv6_addr"), rs.getString("type"));
            }

            connection.close();
        }
        catch(Exception ex)
        {
//            throw new DAOException(ex);
            System.out.println("ERROR: DataBase return:");
            ex.printStackTrace();
        }
        finally
        {
            closePool();
        }

//      Se il DB non ha nessun attuatore registrato il metodo ritorna una lista con un solo attuatore con indirizzo ZERO.
        if (toRet == null)
            toRet = new Actuator(0, "0", "0");
        System.out.println("DGB     DriverDB: " + toRet.toString());
        return toRet;
    }

    public static boolean registerActuator(Actuator act)
    {
//        First, check presence
        Actuator presence = getActuator(act.getTyre_position(), act.getResource());

//        Gia presente un attuatore
        if(presence != null && presence.getTyre_position() == act.getTyre_position())
        {
            return false;
        }

        StringBuilder setActuator  = new StringBuilder();
        setActuator.append("insert into actuators (type, tyre_position, ipv6_addr) values");
        setActuator.append("(?,?,?)");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(setActuator.toString(), Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, act.getResource());
            preparedStatement.setInt(2, act.getTyre_position());
            preparedStatement.setString(3, act.getAddr());

            preparedStatement.executeUpdate();
            connection.close();
        }
        catch(Exception ex)
        {
//            throw new DAOException(ex);
        }
        finally {
            closePool();
        }

        return true;
    }

    public static void updateStatus(String ip){

        StringBuilder insertTemperature = new StringBuilder();
        insertTemperature.append("update actuators set timestamp = now() " +
                            "where ipv6_addr=? and timestamp >= now() - interval 5 minute\n");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(insertTemperature.toString(), Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, ip);

            preparedStatement.executeUpdate();

            connection.close();

        }
        catch(Exception ex)
        {
//            throw new DAOException(ex);
        }
        finally {

            closePool();
        }
    }
}
