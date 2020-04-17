/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchatmessenger.threads;

import chatmessenger.util.Chat;
import chatmessenger.util.Cliente;
import java.io.IOException;
import java.net.Socket;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import serverchatmessenger.TelaServerController;

/**
 *
 * @author fndca
 */
public class MessageThread implements Runnable
{

    private TelaServerController tela;
    private Cliente cliente;
    private String message;
    private String instruction;

    MessageThread(Socket socket, TelaServerController tela)
    {
        this.tela = tela;
        cliente = new Cliente("Desconhecido", socket);
    }

    @Override
    public void run()
    {
        do
        {
            try
            {
                message = (String) cliente.getInput().readObject();
                instruction = message.substring(0, Chat.SIZE_INSTRUCTION);
                message = message.substring(Chat.SIZE_INSTRUCTION);
                switch(instruction)
                {
                    case Chat.CONNECT:
                        connect();
                        break;
                    case Chat.DISCONNECT:
                        disconnect();
                        break;
                    case Chat.SEND_ONE:
                        sendOne();
                        break;
                    case Chat.SEND_ALL:
                        //sendAll(this.message);
                        sendAll();
                        break;
                }

            }
            catch(Exception e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro ThreadServer");
                alert.setHeaderText("Erro ao distribuir a menssagem" + "\n" + "Erro: " + e.getMessage());

                alert.showAndWait();
            }
        }while(!instruction.equals(Chat.DISCONNECT));
    }

    void disconnect()
    {
        int i;
        for(i = 0; i < tela.getArrayClientes().size(); i++)
        {
            if(tela.getArrayClientes().get(i).getInput().equals(this.cliente.getInput()))
            {
                break;
            }
        }
        tela.getArrayClientes().remove(i);
        refreshClientes();
        message = cliente.getNome() + " saiu da sala"; // mensagem para todos da sala
        sendAll();
        try
        {
            Thread.sleep(1000);
            message = Chat.DISCONNECT + "Cliente desconectado com succeso";
            cliente.getOutput().writeObject(message);
            cliente.getInput().close();
            cliente.getOutput().close();
            cliente.getSocket().close();
        }
        catch(Exception ex)
        {
        }

    }

    public int buscaCliente(String clinome)
    {
        if(tela.getArrayClientes().size() > 0)
        {
            int inicio = 0, fim = tela.getArrayClientes().size() - 1, meio = fim / 2;
            while(inicio < fim && !tela.getArrayClientes().get(meio).getNome().equals(clinome))
            {
                if(tela.getArrayClientes().get(meio).getNome().compareTo(clinome) > 0)
                {
                    inicio = meio - 1;
                }
                else
                {
                    fim = meio + 1;
                }
                meio = (inicio + fim) / 2;
            }
            if(tela.getArrayClientes().get(meio).getNome().equals(clinome))
            {
                return meio;
            }
        }
        return -1;
    }

    private void connect() // atualizar lista
    {
       // if(buscaCliente(message) > -1) // cliente não existe
       // {
            this.message += tela.getNomesIguais();
            tela.NomesIguaisPlusPlus();
       // }
        this.cliente.setNome(message);
        tela.getArrayClientes().add(this.cliente);
        tela.getArrayClientes().sort(new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                Cliente p1 = (Cliente) o1;
                Cliente p2 = (Cliente) o2;
                return p1.getNome().compareTo(p2.getNome());
            }
        });
        try
        {
            this.cliente.getOutput().writeObject(Chat.CONNECT+this.message);
        }
        catch(Exception e)
        {
        }
        this.message += " entrou na sala";
        sendAll();
        refreshClientes();
    }

    private void sendAll()
    {
        Cliente cli;
        this.tela.getTxConversas().appendText(this.message + "\n");
        this.message = Chat.SEND_ALL + this.message;
        for(int i = 0; i < tela.getArrayClientes().size(); i++)
        {
            cli = tela.getArrayClientes().get(i);
            if(!cli.getNome().equals(this.cliente.getNome()))
            {
                try
                {
                    cli.getOutput().writeObject(this.message);
                }
                catch(IOException ex)
                {
                }
            }
        }

    }

    private void sendOne() // instrução + nome do destinatário + §
    {
        int i = this.message.indexOf("§");
        String nomedest = this.message.substring(0, i);
        this.message = this.message.substring(i + 1);
        for(i = 0; i < tela.getArrayClientes().size(); i++)
        {
            if(tela.getArrayClientes().get(i).getNome().equals(nomedest))
            {
                break; // quando i estiver na pos do dest para
            }
        }
        this.tela.getTxConversas().appendText(this.cliente.getNome().toUpperCase() + " DISSE PARA "+nomedest.toUpperCase() + " :"
                +this.message.substring(this.message.indexOf(":")+1) + "\n"); // envia a msg para o textarea do servidor
        try
        {
            tela.getArrayClientes().get(i).getOutput().writeObject((this.instruction + this.message));
        }
        catch(IOException ex)
        {
            Logger.getLogger(MessageThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void refreshClientes()
    {
        this.tela.refresh();
        // atualizando tbv de clientes onlines
        /*Platform.runLater(() ->
        {
            tela.getTbvUsuarios().getItems().clear();
            if(!tela.getArrayClientes().isEmpty())
            {
                tela.getTbvUsuarios().getItems().addAll(tela.getArrayClientes());
            }
            else
            {
                tela.clearNomerIguais();
                tela.getArrayClientes().clear();
            }
        });
        try
        {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex)
        {
        }
        Cliente cli;
        this.message = Chat.USERS_ONLINE;
        for(int i = 0; i < tela.getArrayClientes().size(); i++)
        {
            cli = tela.getArrayClientes().get(i);
            this.message += cli.getNome() + "§"; // gerando string com nome dos clientes onlines
        }
        for(int i = 0; i < tela.getArrayClientes().size(); i++)
        {
            try
            {
                tela.getArrayClientes().get(i).getOutput().writeObject(this.message);
            }
            catch(IOException ex)
            {
            }
        }*/
    }
}
