import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServidorChess extends Thread {
    private Jogador jogador = null;
    private Vector<Jogador> listaJogador = new Vector<Jogador>();
    private ServerSocket servidor = null;
    private int quantidadeJogadores = 2;
    private String toClient;
    private int jogadorDaVez = 0;

    public ServidorChess(int porta) {
        try {
            servidor = new ServerSocket(porta);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("servidor no ar na porta " + porta + " !!");
    }

    public static void main(String[] a) {
        ServidorChess s = new ServidorChess(9999);
        s.start();
    }

    private void removeJogador(Jogador j) {
        listaJogador.remove(j);
    }

    private void addJogador(Jogador j) {

        if (listaJogador.size() < quantidadeJogadores) {
            listaJogador.add(j);
        }
    }

    private int geraNumeroDoJogador() {
        boolean achei = false;

        for (int j = 1; j <= quantidadeJogadores; j++) {
            achei = false;
            for (int i = 0; i < listaJogador.size(); i++) {
                jogador = listaJogador.elementAt(i);
                if (jogador.getNumero() == j) {
                    achei = true;
                }
            }
            if (achei == false)
                return j;
        }
        return -1;
    }

    public void run() {
        while (true) {
            try {

                jogador = new Jogador(servidor.accept(), geraNumeroDoJogador());
            } catch (java.io.IOException e) {
            }

            if (listaJogador.size() < quantidadeJogadores) {
                addJogador(jogador);
                System.out.println("Jogador n# " + jogador.getNumero() + " entrou");
                toClient = "[NewGame][text]" + "[Jogador" + jogador.getNumero();
                System.out.println("Foi enviado [NewGame]");
                jogador.informe(toClient);
                jogador.start();

                sendToOponente(jogador.getNumero() + " ainda nao e a sua vez.", jogador);
            } else {
                jogador.informe("[text]:O grupo ja estah completo.\nTente entrar na partida mais tarde.");
                jogador.informe(">>SERVEROFF");
                jogador.closeConnection();
            }
        }
    }

    private void sendToOponente(String msg, Jogador quemEnvia) {

        for (int i = 0; i < listaJogador.size(); i++) {
            jogador = listaJogador.elementAt(i);
            if ((jogador.getNumero() != quemEnvia.getNumero()) && (msg.length() >= 1)) {
                jogador.informe(msg);
            }
        }

        if ((msg.lastIndexOf("[move]") > 0) && (!(msg.lastIndexOf("[GameOver]") >= 0))) {
            if (!(msg.lastIndexOf("[Again]") >= 0))
                jogadorDaVez++;

            if (jogadorDaVez >= listaJogador.size()) {
                jogadorDaVez = 0;
            }
            for (int i = 0; i < listaJogador.size(); i++) {
                jogador = listaJogador.elementAt(i);

                if (i == jogadorDaVez) {
                    jogador.informe("[YourMove]");
                } else {
                    jogador.informe("[Wait]");
                }

            }
        }
    }

    class Jogador extends Thread {
        private ObjectOutputStream output;
        private ObjectInputStream inStream;
        private Socket connection;
        private int numero;

        public Jogador(Socket s, int n) {
            numero = n;
            connection = s;
            try {
                output = new ObjectOutputStream(s.getOutputStream());
                inStream = new ObjectInputStream(s.getInputStream());
                output.flush();
            } catch (IOException e) {
            }
        }

        private int getNumero() {
            return numero;
        }

        public void run() {
            String movimento;
            int x;
            while (true) {
                movimento = ouvir(true);
                x = movimento.indexOf("[exit]");
                if (x >= 0) {
                    ServidorChess.this.removeJogador(this);
                } else {
                    ServidorChess.this.sendToOponente(movimento, this);
                }
            }
        }

        private void informe(String m) {
            try {
                output.writeObject(m);
                output.flush();
            } catch (java.io.IOException e) {
                System.out.println("Erro de envio de mensagem");
                this.closeConnection();
                interrupt();
            }
        }

        private String ouvir(boolean eco) {
            String saida = new String("");
            try {
                saida = (String) inStream.readObject();
                if (eco)
                    System.out.println("ouvi " + saida);
            } catch (IOException e) {
                saida = "";
            } catch (java.lang.ClassNotFoundException clerr) {
                saida = "";
            }
            return saida;
        }

        private void closeConnection() {
            {
                try {
                    ServidorChess.this.removeJogador(this);
                    if (this.isAlive()) {
                        this.interrupt();
                        connection.close();
                    }
                } catch (Exception exp) {
                    System.out.println("Cliente ausente");
                }
            }
        }

    }
}

// Instrucao => instrucao enviada ao cliente.
// "[NewGame]" => cria as pecas e posiciona-as no tabuleiro.
// "[Jogador <x>" => Envia o numero do jogador
// "[text]:" => apresentar ao cliente o texto a seguir.
// "[YourMove]"=> sua vez de jogar. O tabuleiro estarah sensivel ao clique do
// mouse.
// "[Wait]" => O tabuleiro nao aceitarah o clique do mouse. O jogador estah
// esperando a sua vez.
// "[move]" => Serah enviada uma jogada
