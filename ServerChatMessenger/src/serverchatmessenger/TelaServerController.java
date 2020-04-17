/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchatmessenger;

import chatmessenger.util.Chat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import chatmessenger.util.Cliente;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import static javafx.scene.paint.Color.color;
import static javafx.scene.paint.Color.color;
import javafx.stage.Stage;
import serverchatmessenger.threads.ServerThread;

/**
 *
 * @author fndca
 */
public class TelaServerController implements Initializable
{

    private boolean ativo;
    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ServerSocket server;
    private Socket socket;
    private ServerThread newThread;
    private Thread threadmain;
    private String message;
    private int nomesiguais;
    private double coordX, coordY;

    @FXML
    private TextArea txarea;
    @FXML
    private Button btavisar;
    @FXML
    private Button btavisartodos;
    @FXML
    private TableView<Cliente> tbvusuarios;
    @FXML
    private TableColumn<String, String> colip;
    @FXML
    private TableColumn<String, String> colusuario;
    @FXML
    private Button btconectar;
    @FXML
    private Button btdesconectar;
    @FXML
    private Label lbstatus;
    @FXML
    private Button btX;
    @FXML
    private TextArea txconversas;
    @FXML
    private Button btdisconnectuser;
    @FXML
    private Button btminimizar;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
        this.nomesiguais = 0;
        this.colip.setCellValueFactory(new PropertyValueFactory<>("ip"));
        this.colusuario.setCellValueFactory(new PropertyValueFactory<>("nome"));
        habilitar("D");
    }

    public int getNomesIguais()
    {
        return this.nomesiguais;
    }

    public void NomesIguaisPlusPlus()
    {
        this.nomesiguais++;
    }

    public void clearNomerIguais()
    {
        this.nomesiguais = 0;
    }

    public void habilitar(String acao) // C - CONNECTED D - DISCONNETED
    {
        this.btavisar.setDisable(acao.equalsIgnoreCase("d"));
        this.btavisartodos.setDisable(acao.equalsIgnoreCase("d"));
        this.tbvusuarios.setDisable(acao.equalsIgnoreCase("d"));
        this.btdisconnectuser.setDisable(acao.equalsIgnoreCase("d"));
        if(acao.equalsIgnoreCase("D"))
        {
            this.nomesiguais = 0;
            this.tbvusuarios.getItems().clear();
            this.clientes.clear();
            this.txarea.clear();
            this.txconversas.clear();
            this.lbstatus.setText("Servidor Offline");
        }
        else
        {
            this.lbstatus.setText("Servidor Online");
        }
        this.btconectar.setDisable(acao.equalsIgnoreCase("c"));
        this.btdesconectar.setDisable(acao.equalsIgnoreCase("d"));
        this.txarea.setDisable(acao.equalsIgnoreCase("d"));
        this.ativo = acao.equalsIgnoreCase("c");
    }

    public boolean isAtivo()
    {
        return this.ativo;
    }

    @FXML
    private void digitarMensagem(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER && !this.txarea.getText().isEmpty())
        {
            if(this.tbvusuarios.getSelectionModel().getSelectedIndex() > -1)
            {
                clkAvisar(null);
            }
            else
            {
                clkAvisarTodos(null);
            }
        }
    }

    @FXML
    private void clkAvisar(ActionEvent event)
    {
        if(!this.txarea.getText().isEmpty())
        {
            int index = tbvusuarios.getSelectionModel().getSelectedIndex();
            if(index > -1)
            {
                Cliente cli = tbvusuarios.getItems().get(index);
                String msg = Chat.SEND_ONE
                        + "CHATMESSENGER DISSE PARA VOCÊ: " + this.txarea.getText();
                try
                {
                    tbvusuarios.getItems().get(index).getOutput().writeObject(msg);
                }
                catch(IOException ex)
                {
                }
                tbvusuarios.getSelectionModel().clearSelection();
            }
            this.txarea.clear();
        }

    }

    @FXML
    private void clkAvisarTodos(ActionEvent event)
    {
        if(!this.txarea.getText().isEmpty())
        {
            String msg = Chat.SEND_ALL + "ChatMessenger disse: " + this.txarea.getText();
            sendAll(msg);
            this.txarea.clear();
        }
    }

    @FXML
    private void clkConectar(ActionEvent event)
    {
        try
        {
            this.ativo = true;
            this.server = new ServerSocket(9090);
            this.newThread = new ServerThread(socket, server, this);
            this.threadmain = new Thread(this.newThread);
            this.threadmain.start();
            habilitar("C");
        }
        catch(IOException ex)
        {
            Logger.getLogger(TelaServerController.class.getName()).log(Level.SEVERE, null, ex);
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("ERRO DE CONEXÃO!");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void clkFechar(ActionEvent event)
    {
        clkDesconectar(event);
        Platform.exit();
    }

    @FXML
    private void clkDesconectar(ActionEvent event)
    {
        if(this.ativo)
        {
            try
            {
                this.message = Chat.SERVER_OFF;
                for(int i = 0; i < this.clientes.size(); i++)
                {
                    this.clientes.get(i).getOutput().writeObject(this.message);
                }
                Thread.sleep(50); // tempo de espera para clientes serem encerrados
                server.close(); //fechando conexão
                habilitar("D");
                threadmain.stop();
            }
            catch(Exception e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("ERRO EM DESCONECTAR O SERVIDOR: " + e.getClass());
                alert.showAndWait();
            }
        }
    }

    public TableView<Cliente> getTbvUsuarios()
    {
        return this.tbvusuarios;
    }

    public ArrayList<Cliente> getArrayClientes()
    {
        return this.clientes;
    }

    @FXML
    private void clkDisconnectUser(ActionEvent event)
    {
        int index = this.tbvusuarios.getSelectionModel().getSelectedIndex();
        if(index > -1)
        {
            try
            {
                this.clientes.get(index).getOutput().writeObject(Chat.DISCONNECT);
            }
            catch(IOException ex)
            {
                Logger.getLogger(TelaServerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.message = Chat.SEND_ALL + "ChatMessengar disse: " + this.clientes.get(index).getNome() + " foi desconectado";
            this.clientes.remove(index);
            this.tbvusuarios.getItems().clear();
            this.tbvusuarios.getItems().addAll(this.clientes);
            for(Cliente cliente : clientes)
            {
                try
                {
                    cliente.getOutput().writeObject(this.message);
                }
                catch(Exception e)
                {
                }

            }
            String msg = Chat.USERS_ONLINE;
            for(Cliente cliente : clientes)
            {
                msg += cliente.getNome() + "§";
            }
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
            }
            sendAll(msg);
        }
    }

    public void refresh()
    {
        // atualizando tbv de clientes onlines
        Platform.runLater(() ->
        {
            getTbvUsuarios().getItems().clear();
            if(!getArrayClientes().isEmpty())
            {
                getTbvUsuarios().getItems().addAll(getArrayClientes());
            }
            else
            {
                clearNomerIguais();
                getArrayClientes().clear();
            }
        });
        try
        {
            Thread.sleep(100);
        }
        catch(InterruptedException ex)
        {
        }
        Cliente cli;
        this.message = Chat.USERS_ONLINE;
        for(int i = 0; i < getArrayClientes().size(); i++)
        {
            cli = getArrayClientes().get(i);
            this.message += cli.getNome() + "§"; // gerando string com nome dos clientes onlines
        }
        for(int i = 0; i < getArrayClientes().size(); i++)
        {
            try
            {
                getArrayClientes().get(i).getOutput().writeObject(this.message);
            }
            catch(IOException ex)
            {
            }
        }
    }

    @FXML
    private void clkTbvUsuarios(MouseEvent event)
    {// habilitar botão desconectarUsuario
        //if()
    }
    
    @FXML
    private void mouseInButtonX(MouseEvent event)
    {// mudar cor do botão
        this.btX.setStyle(
                "-fx-opacity: 100;"
                + "-fx-background-image: url(\"icons/fechar2.png\");"
                + "-fx-background-color: red;"
        );
    }

    @FXML
    private void mouseOutButtonX(MouseEvent event)
    {
        this.btX.setStyle(
                "-fx-opacity: 0.5;"
                + "-fx-background-image: url(\"icons/fechar1.png\");"
                + "-fx-background-color: transparent;"
        );
    }

    @FXML
    private void mouseOutButtonMinimize(MouseEvent event)
    {
        this.btminimizar.setStyle(
                "-fx-opacity: 0.5;"
                + "-fx-background-image: url(\"icons/minimizar.png\");"
                + "-fx-background-color: transparent;"
        );
    }

    @FXML
    private void mouseInButtonMinimize(MouseEvent event)
    {
        this.btminimizar.setStyle(
                "-fx-opacity: 100;"
                + "-fx-background-image: url(\"icons/minimizar.png\");"
                + "-fx-background-color: lightgray;"
        );
    }

    @FXML
    private void clkMinimizar(ActionEvent event)
    {
        Stage stage = (Stage) btminimizar.getScene().getWindow();
        stage.setIconified(true);
    }

    public TextArea getTxConversas()
    {
        return this.txconversas;
    }

    private void sendAll(String msg)
    {
        for(Cliente cli : clientes)
        {
            try
            {
                cli.getOutput().writeObject(msg);
            }
            catch(IOException ex)
            {
            }
        }
    }

    @FXML
    private void evtArrastarJanela(MouseEvent event)
    {
        Stage stage = ((Stage) ((Node) event.getSource()).getScene().getWindow());
        stage.setX(event.getScreenX() + coordX);
        stage.setY(event.getScreenY() + coordY);
    }

    @FXML
    private void clickBaraJanela(MouseEvent event)
    {
        Stage stage = ((Stage) ((Node) event.getSource()).getScene().getWindow());
        coordX = stage.getX() - event.getScreenX();
        coordY = stage.getY() - event.getScreenY();
    }
}
