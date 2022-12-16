package Socket.HTTP.Server;
import java.net.Socket;
import java.util.*;

public class DataBase {

    Map<String, Boolean> clients;

    public DataBase()
    {
        System.out.println( "Created Data Base" );
        clients = new HashMap<>();
    }

    public void addClient( Socket client )
    {
        for ( Map.Entry<String, Boolean> i : clients.entrySet() )
            if( i.getKey().equals(client.getInetAddress().toString()) ) {
                System.out.println("This clint already in database");
                return;
            }
        clients.put(client.getInetAddress().toString(), false);
        System.out.println("Added new client " + client.getInetAddress());
        System.out.println("List of clients: " + clients);
    }

    public boolean isClientAuto( Socket client )
    {
        for ( Map.Entry<String, Boolean> i : clients.entrySet() )
            if( i.getKey().equals(client.getInetAddress().toString()) ) {
                System.out.println("Client authorized stastus: " + i.getValue());
                return i.getValue();
            }
        return false;
    }

    public void doClientAuto ( Socket client )
    {
        for ( Map.Entry<String, Boolean> i : clients.entrySet() )
            if( i.getKey().equals(client.getInetAddress().toString()) ) {
                System.out.println("This client is now authorized");
                i.setValue(true);
            }
    }


}
