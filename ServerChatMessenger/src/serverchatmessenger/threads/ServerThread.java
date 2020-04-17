/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchatmessenger.threads;

import chatmessenger.util.Cliente;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import serverchatmessenger.TelaServerController;

/**
 *
 * @author fndca
 */
public class ServerThread implements Runnable
{
    private TelaServerController tela;
    private Socket socket;
    private ServerSocket server;
    private MessageThread threadMessage;
    private Thread newThread;
    
    public ServerThread(Socket socket, ServerSocket server, TelaServerController tela)
    {
        this.socket = socket;
        this.server = server;
        this.tela = tela;
    }
    
    @Override
    public void run()
    {
       do
       {
            try
            {
                socket  = server.accept();
                threadMessage = new MessageThread(socket, tela);
                newThread = new Thread(threadMessage);
                newThread.start();
            }
            catch(Exception e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro ThreadServer");
                alert.setHeaderText("Erro na classe ThreadServer" + "\n" + "Erro: " + e.getMessage());
                alert.showAndWait();
            }
       } while(tela.isAtivo());
    }

    
}
