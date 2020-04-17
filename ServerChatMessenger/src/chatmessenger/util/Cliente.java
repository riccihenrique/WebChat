package chatmessenger.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author fndca
 */
public class Cliente
{

    private String nome, ip;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    

    public Cliente(String nome, Socket socket)
    {
        this.ip = socket.getInetAddress().getHostAddress();
        this.nome = nome;
        this.socket = socket;
        try
        {
            this.input = new ObjectInputStream(socket.getInputStream());
            this.output = new ObjectOutputStream(socket.getOutputStream());
        }
        catch(IOException ex){}
    }

    public String getIp()
    {
        return this.ip;
    }

    public String getNome()
    {
        return nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public ObjectInputStream getInput()
    {
        return input;
    }

    public ObjectOutputStream getOutput()
    {
        return output;
    }


}
