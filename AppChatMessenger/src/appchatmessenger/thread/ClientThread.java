/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appchatmessenger.thread;

import appchatmessenger.TelaController;
import chatmessenger.util.Chat;
import chatmessenger.util.Cliente;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author fndca
 */
public class ClientThread implements Runnable
{

    private TelaController tela;
    private Socket client;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message, instruction;

    public ClientThread(Socket client, ObjectOutputStream output, ObjectInputStream input, TextArea taDialog, TelaController tela)
    {
        this.client = client;
        this.output = output;
        this.input = input;
        this.tela = tela;
    }

    @Override
    public void run()
    {
        try
        {
            do
            {
                    message = (String)input.readObject();
                    instruction = message.substring(0, Chat.SIZE_INSTRUCTION);
                    message = message.substring(Chat.SIZE_INSTRUCTION);
                    switch(instruction)
                    {
                        case Chat.CONNECT: connect(); break;
                        case Chat.SEND_ONE: 
                        case Chat.SEND_ALL: send(); break;
                        case Chat.USERS_ONLINE: refreshListOnlines(); break;
                        //case Chat.SERVER_OFF: break;
                    }

            }while(!instruction.equals(Chat.SERVER_OFF) && !instruction.equals(Chat.DISCONNECT));
            
            output.close();
            input.close();
            client.close();
            tela.habilitar("D");
        }
        catch(Exception e)
        {
            System.out.println("Erro em ClientThread\n" + e.getMessage());
        }
    }

    private void refreshListOnlines()
    {
        String[] list = this.message.split("§");
        for(int i = 0; i < list.length; i++)
        {
            
        }
        tela.getUsersOnline().getItems().clear();
        tela.getUsersOnline().getItems().addAll(list);
    }

    private void send()
    {
        Platform.runLater(() ->
        {
            tela.getTextConversa().appendText(message + "\n");
        });
    }

    private void connect()
    {
        tela.setNameCliente(message);
    }
}
