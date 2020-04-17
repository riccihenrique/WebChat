/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatmessenger.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 *
 * @author fndca
 */
public class Cliente implements Serializable
{

    private String nome, ip;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public String getNome()
    {
        return nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
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

    public void setInput(ObjectInputStream input)
    {
        this.input = input;
    }

    public ObjectOutputStream getOutput()
    {
        return output;
    }

    public void setOutput(ObjectOutputStream output)
    {
        this.output = output;
    }

    

}
