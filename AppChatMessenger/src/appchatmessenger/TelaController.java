package appchatmessenger;

import appchatmessenger.thread.ClientThread;
import chatmessenger.util.Chat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author fndca
 */
public class TelaController implements Initializable
{

    private String message;
    private Socket socket;
    private ClientThread socketThreadCliente;
    private Thread threadmain;
    private ObjectOutputStream output; /// a tela apenas envia mensagens, quem recebe é a thread
    private ObjectInputStream input;
    private int online;
    private String nome;
    private double coordX, coordY;
    @FXML
    private TextField txnome;
    @FXML
    private Button btconectar;
    @FXML
    private Button btdesconectar;
    @FXML
    private TextField txmensagem;
    @FXML
    private Button btenviar;
    @FXML
    private TextArea txconversa;
    @FXML
    private ListView<String> listonlines;
    @FXML
    private Button btfechar;
    @FXML
    private Button btonlines;
    @FXML
    private Button btminimizar;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
        habilitar("D");
        this.txconversa.setEditable(false);
        this.message = "";
        this.online = 0;
    }

    @FXML
    private void digitarNome(KeyEvent event)
    {
        if (event.getCode() == KeyCode.ENTER && !this.txnome.getText().isEmpty())
        {
            clkConectar(null);
        }
    }

    @FXML
    private void clkConectar(ActionEvent event)
    {
        if (!this.txnome.getText().trim().isEmpty())
        {
            try
            {
                socket = new Socket("localhost", 9090);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                this.socketThreadCliente = new ClientThread(socket, output, input, txconversa, this);
                //this.socketThreadCliente = new ClientThread(this);
                this.threadmain = new Thread(this.socketThreadCliente);
                this.threadmain.start();
                this.nome = this.txnome.getText();
                this.message = (Chat.CONNECT + this.nome);
                //this.message = ChatMessage.CONNECT + this.txnome.getText();
                this.online = -1;
                output.writeObject(this.message); // quando executar o connect altera o valor de online
                //try{Thread.sleep(1000);}catch(Exception e){}
                //if(this.online == -1)
                //    try{Thread.sleep(1000);}catch(Exception e){}
                //if(this.getOnline())
                //{
                this.txconversa.appendText("Você entrou na sala\n");
                habilitar("C");
                /*}
                else
                {
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setHeaderText("Erro ao tentar conectar");
                    a.setContentText("Já existe um usuário conectado com esse nome\n tente novamente com outro");
                    a.showAndWait();
                }*/
            } catch (IOException ex)
            {
                Logger.getLogger(TelaController.class.getName()).log(Level.SEVERE, null, ex);
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Erro na conexão com o servidor\n" + ex.getMessage());
                a.showAndWait();
            }
        } else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Digite um nome para entrar na sala");
            alert.showAndWait();
            this.txnome.requestFocus();
        }
    }

    @FXML
    private void clkDesconectar(ActionEvent event)
    {
        disconnect();
    }

    @FXML
    private void digitarMensagem(KeyEvent event)
    {
        if (event.getCode() == KeyCode.ENTER && !this.txmensagem.getText().isEmpty())
        {
            clkEnviar(null);
        }
    }

    @FXML
    private void clkEnviar(ActionEvent event)
    {
        if (!this.txmensagem.getText().isEmpty())
        {
            boolean enviar = false;
            int index = listonlines.getSelectionModel().getSelectedIndex();
            if (index > -1) // um usuário foi selecionado
            {
                //if (!this.listonlines.getSelectionModel().getSelectedItem().equals(this.txnome.getText()))
                //{
                    this.message = Chat.SEND_ONE + this.listonlines.getSelectionModel().getSelectedItem()
                            + "§" + this.txnome.getText().toUpperCase() + " DISSE PARA VOCÊ: " + this.txmensagem.getText();
                    this.txconversa.appendText("VOCÊ DISSE PARA "
                            + this.listonlines.getSelectionModel().getSelectedItem().toUpperCase()
                            + ": " + this.txmensagem.getText() + "\n");
                    enviar = true;
                //}
                this.listonlines.getSelectionModel().clearSelection();

            } else
            {
                this.message = Chat.SEND_ALL + this.txnome.getText() + " disse: " + this.txmensagem.getText();
                this.txconversa.appendText("Você disse: " + this.txmensagem.getText() + "\n");
                enviar = true;
            }
            if (enviar)
            {
                try
                {
                    this.output.writeObject(this.message);
                    System.out.println("Tela Cliente... ENVIOU mensagem do cliente");
                } catch (IOException ex)
                {
                    System.out.println("Tela Cliente... Erro ao enviar mensagem do cliente");
                    Logger.getLogger(TelaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.txmensagem.clear();

        }
    }

    public ListView<String> getUsersOnline()
    {
        return this.listonlines;
    }

    public TextArea getTextConversa()
    {
        return this.txconversa;
    }

    public void habilitar(String acao)
    {
        this.btconectar.setDisable(acao.equalsIgnoreCase("C"));
        this.btdesconectar.setDisable(acao.equalsIgnoreCase("D"));
        this.btenviar.setDisable(acao.equalsIgnoreCase("D"));
        this.txconversa.setDisable(acao.equalsIgnoreCase("D"));
        this.txmensagem.setDisable(acao.equalsIgnoreCase("D"));
        this.listonlines.setDisable(acao.equalsIgnoreCase("D"));
        this.txnome.setDisable(acao.equalsIgnoreCase("C"));
        this.txconversa.setDisable(acao.equalsIgnoreCase("D"));
        if (acao.equalsIgnoreCase("D"))
        {
            this.txnome.setText(this.nome);
            this.listonlines.getItems().clear();
            this.txconversa.clear();
            this.txmensagem.clear();
            this.btconectar.requestFocus();
        } else
        {
            this.txmensagem.requestFocus();
        }
    }

    public void disconnect()
    {
        if (!this.message.isEmpty())
        {
            //Alert a = new Alert(Alert.AlertType.INFORMATION);
            //a.setTitle("Desconexão");
            this.message = Chat.DISCONNECT + this.txnome.getText();
            try
            {
                this.output.writeObject(this.message);
                this.output.close();
                this.socket.close();
                this.threadmain.stop();
                habilitar("D");
                //a.setContentText(this.txnome.getText() + " desconectado com sucesso");
                //a.showAndWait();
            } catch (IOException ex)
            {
                //a.setAlertType(Alert.AlertType.ERROR);
                //a.setContentText("Erro ao enviar mensagem de desconexão para o servidor\n" + ex.getMessage());
                //a.showAndWait();
            }
            this.message = "";
        }

    }

    @FXML
    private void clkFechar(ActionEvent event)
    {
        disconnect();
        Platform.exit();
    }

    public void setNameCliente(String nome)
    {
        this.txnome.setText(nome);
    }

    @FXML
    private void clkListOnlines(MouseEvent event)
    {
        if (this.listonlines.getSelectionModel().getSelectedItem().equals(this.txnome.getText()))
            this.listonlines.getSelectionModel().clearSelection();
    }

    @FXML
    private void evtArrastarJanela(MouseEvent event)
    {
        Stage stage = ((Stage)((Node)event.getSource()).getScene().getWindow());
        stage.setX(event.getScreenX()+coordX);
        stage.setY(event.getScreenY()+coordY);
    }

    @FXML
    private void clkBarraJanela(MouseEvent event)
    {
        Stage stage = ((Stage)((Node)event.getSource()).getScene().getWindow());
        coordX = stage.getX() - event.getScreenX();
        coordY = stage.getY() - event.getScreenY(); 
    }

    @FXML
    private void mouseOutBtFechar(MouseEvent event)
    {
        this.btfechar.setStyle(
                "-fx-opacity: 0.5;"
                + "-fx-background-image: url(\"icons/fechar1.png\");"
                + "-fx-background-color: transparent;"
        );
    }

    @FXML
    private void mouseInBtFechar(MouseEvent event)
    {
        this.btfechar.setStyle(
                "-fx-opacity: 100;"
                + "-fx-background-image: url(\"icons/fechar2.png\");"
                + "-fx-background-color: red;"
        );
    }

    @FXML
    private void mouseOutMinimizar(MouseEvent event)
    {
        this.btminimizar.setStyle(
                "-fx-opacity: 0.5;"
                + "-fx-background-image: url(\"icons/minimizar.png\");"
                + "-fx-background-color: transparent;"
        );
    }

    @FXML
    private void mouseInMinimizar(MouseEvent event)
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

}
