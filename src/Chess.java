import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;


//-------------------------------------class de eventos
class Acoes extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}

// --------------------------------------class principal
public class Chess extends JFrame {
    private static final long serialVersionUID = 7526471155622776147L;
    private final Joga conteudo = new Joga();
    private JMenuBar menu = new JMenuBar();

    public Chess() {
        setSize(660, 700);
        addWindowListener(new Acoes());
        conteudo.addMouseListener(new GerenteMouse());
        Container fundo = getContentPane();
        JMenu Arquivo = new JMenu("Arquivo");
        Arquivo.setMnemonic('A');
        JMenuItem novo = new JMenuItem("Novo Jogo");
        novo.setMnemonic('N');
        novo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                novoJogo();
            }
        });
        JMenuItem show = new JMenuItem("SHOW");
        show.setMnemonic('S');
        show.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                conteudo.Posiciona();
            }
        });
        JMenuItem check = new JMenuItem("CHECK!");
        check.setMnemonic('C');
        check.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                conteudo.sendData("[check]");
            }
        });
        JMenuItem desistir = new JMenuItem("Desistir");
        desistir.setMnemonic('D');
        desistir.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                conteudo.sendData("[fim]");
                conteudo.sendData("[exit]");
            }
        });
        Arquivo.add(novo);
        menu.add(Arquivo);
        menu.add(show);
        menu.add(check);
        menu.add(desistir);
        setJMenuBar(menu);
        conteudo.setBackground(Color.WHITE);
        fundo.add(conteudo);
    }

    public static void main(String[] lol) {
        Chess b = new Chess();
        b.setVisible(true);
    }

    public void novoJogo() {
        conteudo.paintComponent(conteudo.getGraphics());
    }

    public void realizaJogada(int x, int y, int x1, int y1) {
        conteudo.move(conteudo.getGraphics(), x, y, x1, y1);
    }

    // ----------------------------------------------------------------nova class
    class GerenteMouse extends MouseAdapter {
        private int cliqueX, cliqueY, cliqueX1, cliqueY1, X, Y;

        public GerenteMouse() {

        }

        public void mouseReleased(MouseEvent evt) {
            X = evt.getX();
            Y = evt.getY();
            System.out.println("(" + X + ";" + Y + ")");
            if ((cliqueX != 0) && (cliqueY != 0)) {
                cliqueX1 = X;
                cliqueY1 = Y;
            } else {
                cliqueX = X;
                cliqueY = Y;
            }
            if (cliqueX1 != 0) {
                Chess.this.realizaJogada(cliqueX, cliqueY, cliqueX1, cliqueY1);
                cliqueX = 0;
                cliqueY = 0;
                cliqueX1 = 0;
                cliqueY1 = 0;

            }

        }
    }
}

// ----------------------------------------------------------------nova class
class Joga extends JPanel {
    private static final long serialVersionUID = 7526471155622776147L;
    private static String matriz[][][] = new String[12][12][2];
    protected Cliente evt;
    private int x;
    private int y;
    private int x1;
    private int y1;
    private BufferedImage img;
    private boolean noAr = false;
    private String cor = new String();
    private String peca = new String();
    private String time = new String();
    private regras reg = new regras();

    public Joga() {
        evt = new Cliente("127.0.0.1", "9999");
        evt.start();
        Dimension d = getSize();
        int clientWidth = d.width;
        int clientHeight = d.height;
        setSize(clientWidth, clientHeight);
    }

    public void setTime(String a) {
        time = a;
    }

    public void setNoAr(boolean a) {
        noAr = a;
    }

    public int getX() {
        return x;
    }

    public void setX(int a) {
        x = a;
    }

    public int getY() {
        return y;
    }

    public void setY(int a) {
        y = a;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int a) {
        x1 = a;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int a) {
        y1 = a;
    }

    public String getMatriz(int a, int b, int c) {
        String saida = new String();
        saida = matriz[a][b][c];
        return saida;
    }

    public void dimenciona() {
        Dimension d = getSize();
        int clientWidth = d.width;
        int clientHeight = d.height;
        setSize(clientWidth, clientHeight);
    }

    public void move(Graphics g, int _x, int _y, int _x1, int _y1) {
        x = _x;
        y = _y;
        x1 = _x1;
        y1 = _y1;
        padronizaCasa();
        if (noAr == false) {
            System.out.println("\nAINDA NAO EH SUA VEZ...\n");
        }
        if ((noAr == true) && (reg.identifica(teleport(x), teleport(y),
                teleport(x1), teleport(y1), matriz[teleport(x)][teleport(y)][0],
                matriz[teleport(x1)][teleport(y1)][0], time) == true)) {
            System.out.println("noAr=" + noAr);
            peca = matriz[teleport(x)][teleport(y)][0];
            System.out.println(peca);
            if (peca.lastIndexOf("vazio") > -1) {
                System.out.println("\nVoce clicou em um lugar vazio:	Selecione uma pe�a para jogar.");
            } else if (peca.lastIndexOf(time) < 0) {
                System.out.println("\nEsta pe�a nao e sua:	Selecione uma pe�a para jogar.");
            } else {
                System.out.println("[move]" + teleport(x) + teleport(y) + teleport(x1) + teleport(y1));
                evt.sendData("[move]" + teleport(x) + teleport(y) + teleport(x1) + teleport(y1));
                executaJogada(g);
                setNoAr(false);
            }
        }

    }

    public void padronizaCasa() {
        for (int a = 0; a <= 567; a = a + 81) {
            if ((x > a) && (x < a + 81)) {
                x = a;
            }
            if ((x1 > a) && (x1 < a + 81)) {
                x1 = a;
            }
        }
        for (int b = 0; b <= 567; b = b + 81) {
            if ((y > b) && (y < b + 81)) {
                y = b;
            }
            if ((y1 > b) && (y1 < b + 81)) {
                y1 = b;
            }
        }
    }

    public void executaJogada(Graphics g) {
        // paintCasa(g,x,y);
        matriz[teleport(x1)][teleport(y1)][0] = matriz[teleport(x)][teleport(y)][0];
        matriz[teleport(x)][teleport(y)][0] = "vazio";
        // paintComponent2(g,x1,y1);
        paintCasa(g, x, y);
        Posiciona();
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
    }

    public void paintCasa(Graphics g, int a, int b) {
        cor = matriz[teleport(a)][teleport(b)][1];
        if (cor.lastIndexOf("P") > -1) {
            g.setColor(new Color(0, 100, 200));
            g.fillRect(x, y, 81, 81);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, 81, 81);
        }
    }

    public void paintImage(Graphics g, int a, int b) {
        System.out.println("paintComponent ATIVADO");
        cor = matriz[teleport(a)][teleport(b)][1];
        String lol = "src/resource/" + peca + cor + ".jpg";
        System.out.println(lol);
        g.drawImage(ImageBuffer(lol), a, b, null);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cont2 = 81;
        int x = 81;
        g.setColor(new Color(0, 100, 200));
        for (int cont = 0; cont < 8; cont++) {
            g.fillRect(x * 0 + cont2, x * cont, x, x);
            g.fillRect(x * 2 + cont2, x * cont, x, x);
            g.fillRect(x * 4 + cont2, x * cont, x, x);
            g.fillRect(x * 6 + cont2, x * cont, x, x);
            if (cont2 == 0) {
                cont2 = 81;
            } else {
                cont2 = 0;
            }
        }
        g.draw3DRect(0, 0, 648, 648, true);
    }

    public Image ImageBuffer(String nome) {

        System.out.println(nome);
        URL imageSrc = null;
        try {
            imageSrc = ((new File(nome)).toURI()).toURL();
            System.out.println("URL salva");
        } catch (MalformedURLException e) {
            System.out.println("Defito na url");
        }
        try {
            img = ImageIO.read(imageSrc);
        } catch (IOException e) {
            System.out.println("A imagem nao pode ser lida.");
            System.exit(1);
        }
        return img;
    }

    public void constroiMatriz() {
        int cont2 = 0;
        for (int b = 0; b < 2; b++) {
            for (int a = 0; a < 12; a++) {
                matriz[b][a][0] = null;
                matriz[b][a][1] = null;
            }
        }
        for (int b = 10; b < 12; b++) {
            for (int a = 0; a < 12; a++) {
                matriz[b][a][0] = null;
                matriz[b][a][1] = null;
            }
        }
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 12; b++) {
                matriz[b][a][0] = null;
                matriz[b][a][1] = null;
            }
        }
        for (int a = 10; a < 12; a++) {
            for (int b = 0; b < 12; b++) {
                matriz[b][a][0] = null;
                matriz[b][a][1] = null;
            }
        }

        for (int b = 2; b < 10; b++) {
            for (int a = 2; a < 10; a++) {
                matriz[a][b][0] = "vazio";
            }
        }
        for (int cont = 2; cont < 10; cont++) {
            matriz[2 + cont2][cont][1] = "B";
            matriz[4 + cont2][cont][1] = "B";
            matriz[6 + cont2][cont][1] = "B";
            matriz[8 + cont2][cont][1] = "B";
            if (cont2 == 0) {
                cont2 = 1;
            } else {
                cont2 = 0;
            }
        }
        cont2 = 1;
        for (int cont = 2; cont < 10; cont++) {
            matriz[2 + cont2][cont][1] = "P";
            matriz[4 + cont2][cont][1] = "P";
            matriz[6 + cont2][cont][1] = "P";
            matriz[8 + cont2][cont][1] = "P";
            if (cont2 == 0) {
                cont2 = 1;
            } else {
                cont2 = 0;
            }
        }

    }

    public int teleport(int a) {
        int saida = 0;
        if (a == 0) {
            saida = 2;
        }
        if (a == 81) {
            saida = 3;
        }
        if (a == 162) {
            saida = 4;
        }
        if (a == 243) {
            saida = 5;
        }
        if (a == 324) {
            saida = 6;
        }
        if (a == 405) {
            saida = 7;
        }
        if (a == 486) {
            saida = 8;
        }
        if (a == 567) {
            saida = 9;
        }
        return saida;
    }

    public void novoJogo() {

        for (int a = 2; a < 10; a++) {
            matriz[a][3][0] = "peaopreto";
            matriz[a][8][0] = "peaobranco";

        }
        matriz[9][2][0] = "torrepreto";
        matriz[8][2][0] = "cavalopreto";
        matriz[7][2][0] = "bispopreto";
        matriz[5][2][0] = "rainhapreto";
        matriz[6][2][0] = "reipreto";
        matriz[4][2][0] = "bispopreto";
        matriz[3][2][0] = "cavalopreto";
        matriz[2][2][0] = "torrepreto";

        matriz[9][9][0] = "torrebranco";
        matriz[8][9][0] = "cavalobranco";
        matriz[7][9][0] = "bispobranco";
        matriz[5][9][0] = "rainhabranco";
        matriz[6][9][0] = "reibranco";
        matriz[4][9][0] = "bispobranco";
        matriz[3][9][0] = "cavalobranco";
        matriz[2][9][0] = "torrebranco";
        System.out.println("Tenta acionar o Posicionamento");
        Posiciona();
        System.out.println("Posicionamento acionado");
    }

    public void Posiciona() {
        for (int b = 2; b < 10; b++) {
            System.out.println("B=" + b);
            for (int a = 2; a < 10; a++) {
                System.out.println("A=" + a);
                peca = matriz[a][b][0];
                System.out.println("peca=" + peca);
                cor = matriz[a][b][1];
                if (peca.lastIndexOf("vazio") > -1) {
                    System.out.println("vazio");

                } else {
                    System.out.println("Tentou printar o componente");
                    paintImage(this.getGraphics(), (a - 2) * 81, (b - 2) * 81);
                }
            }
        }
    }

    public void sendData(String a) {
        evt.sendData(a);
    }

    // g.drawImage(ImageBuffer("ReiPretoP.jpg"),0,0,null);

    // ----------------------------------------------------------------------------------
    class Cliente extends Thread {
        private Socket con;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private int p;

        public Cliente(String endereco, String porta) {

            Integer i = Integer.valueOf(porta);
            try {
                con = new Socket(InetAddress.getByName(endereco), i.intValue());
                output = new ObjectOutputStream(con.getOutputStream());
                InputStream s = con.getInputStream();
                input = new ObjectInputStream(s);
            } catch (java.io.IOException er1) {
                System.out.println(er1.getMessage());
            }
        }

        public void run() {
            String s2 = "";
            while (true) {
                try {
                    String mens = (String) input.readObject();
                    if (mens.length() > 1) {
                        p = mens.lastIndexOf("[text]");
                        if (p >= 0) {
                            javax.swing.JOptionPane.showMessageDialog(null, mens.substring(p + 7), "Server Information",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                        ;

                        p = mens.lastIndexOf("[NewGame]");
                        if (p >= 0) {
                            constroiMatriz();
                            novoJogo();
                            System.out.println(mens);
                        }
                        p = mens.lastIndexOf("[Jogador2");
                        if (p >= 0) {
                            System.out.println(mens);
                            setTime("branco");
                            setNoAr(true);
                        }
                        p = mens.lastIndexOf("[Jogador1");
                        if (p >= 0) {
                            System.out.println(mens);
                            setTime("preto");
                        }
                        p = mens.lastIndexOf("[YourMove]");
                        if (p >= 0) {
                            setNoAr(true);
                            System.out.println(mens);
                        }
                        p = mens.lastIndexOf("[Wait]");
                        if (p >= 0) {
                            setNoAr(false);
                            System.out.println(mens);
                        }
                        p = mens.lastIndexOf("[check]");
                        if (p >= 0) {
                            javax.swing.JOptionPane.showMessageDialog(null, "Voce esta em CHECK!!!",
                                    "Server Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                        p = mens.lastIndexOf("[YouLose]");
                        if (p >= 0) {
                            javax.swing.JOptionPane.showMessageDialog(null, "Voce Perdeu!!!", "Server Information",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            sendData("[exit]");
                        }
                        p = mens.lastIndexOf("[fim]");
                        if (p >= 0) {
                            javax.swing.JOptionPane.showMessageDialog(null, "Fim de Jogo", "Server Information",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            sendData("[fim]");
                            sendData("[exit]");
                        }
                        p = mens.lastIndexOf("[move]");
                        if (p >= 0) {
                            System.out.println(mens);
                            s2 = mens.substring(p + 6, p + 7);
                            Joga.this.setX((Integer.parseInt(s2) - 2) * 81);
                            System.out.println(getX());
                            s2 = mens.substring(p + 7, p + 8);
                            Joga.this.setY((Integer.parseInt(s2) - 2) * 81);
                            System.out.println(getY());
                            s2 = mens.substring(p + 8, p + 9);
                            Joga.this.setX1((Integer.parseInt(s2) - 2) * 81);
                            System.out.println(getX1());
                            s2 = mens.substring(p + 9, p + 10);
                            Joga.this.setY1((Integer.parseInt(s2) - 2) * 81);
                            System.out.println(getY1());
                            Joga.this.setNoAr(true);
                            Joga.this.executaJogada(Joga.this.getGraphics());
                        }
                    }
                } catch (java.io.OptionalDataException e) {
                    System.out.println("Erro no desconectar.\n" + e.getMessage());
                } catch (java.lang.ClassNotFoundException e) {
                    System.out.println("Class not found. \n" + e.getMessage());
                } catch (java.io.IOException e) {
                    System.out.println("Erro de IO. \n" + e.getMessage());
                }
            }
        }

        public void sendData(String m) {
            try {
                output.writeObject(m);
                output.flush();
            } catch (IOException e) {
                System.out.println("Nao foi possivel enviar os dados.\n" + e.getMessage());
            }
        }
    }

    // CLASS INTERNA, RESPONS�VEL PELAS REGRAS B�SICAS DO
    // XADREZ-----------------------------------------------------------
    class regras {
        public regras() {
            System.out.println("REGRAS ATIVADAS");
        }

        public boolean identifica(int x, int y, int x1, int y1, String a, String b, String c) {
            System.out.println("\nINICIANDO PROCESSO DE IDENTIFICACAO......");
            boolean saida = false;
            System.out.println("TIME=" + c);
            if (a.lastIndexOf("peaobranco") > -1) {
                saida = peaobranco(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("peaopreto") > -1) {
                saida = peaopreto(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("torre") > -1) {
                saida = torre(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("bispo") > -1) {
                saida = bispo(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("cavalo") > -1) {
                saida = cavalo(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("rainha") > -1) {
                saida = rainha(x, y, x1, y1, b, c);
            }
            if (a.lastIndexOf("rei") > -1) {
                saida = rei(x, y, x1, y1, b, c);
            }
            a = Joga.this.getMatriz(x1, y1, 0);
            if ((saida == true) && (a.lastIndexOf("rei") > -1)) {
                javax.swing.JOptionPane.showMessageDialog(null, "Voce Venceu!!!", "Server Information",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                Joga.this.sendData("[YouLose]");
                Joga.this.sendData("[exit]");
            }
            System.out.println("PROCESSO DE IDENTIFICACAO TERMINADO\n");
            return saida;
        }

        public boolean peaobranco(int x, int y, int x1, int y1, String b, String c) {
            boolean saida;
            boolean teste1;
            boolean teste2;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DO PEAO ATIVADA");
            if (((y - y1) == 1) && ((x1 - x) == 0) && (b.lastIndexOf("vazio") > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else if (((y - y1) == 2) && ((x1 - x) == 0) && (y == 8)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else if (((y - y1) == 1) && (((x - x1) == 1) || ((x - x1) == -1)) && (b.lastIndexOf(_c) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else {
                teste1 = false;
                System.out.println("teste1=" + teste1 + " > x,y,x1,y1=" + x + "," + y + "," + x1 + "," + y1);
            }
            if (b.lastIndexOf(c) < 0) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                teste2 = false;
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);
            return saida;

        }

        public boolean peaopreto(int x, int y, int x1, int y1, String b, String c) {
            boolean saida;
            boolean teste1;
            boolean teste2;
            String _c = new String();
            if (c.lastIndexOf("preto") > -1) {
                _c = "branco";
            } else {
                _c = "preto";
            }
            System.out.println("REGRA DO PEAO ATIVADA");
            if (((y1 - y) == 1) && ((x1 - x) == 0) && (b.lastIndexOf("vazio") > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else if (((y1 - y) == 2) && ((x1 - x) == 0) && (y == 3)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else if (((y1 - y) == 1) && (((x1 - x) == 1) || ((x1 - x) == -1)) && (b.lastIndexOf(_c) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else {
                teste1 = false;
                System.out.println("teste1=" + teste1 + " > x,y,x1,y1=" + x + "," + y + "," + x1 + "," + y1);
            }
            if (b.lastIndexOf(c) < 0) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                teste2 = false;
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);
            return saida;

        }

        public boolean torre(int x, int y, int x1, int y1, String b, String c) {
            boolean teste1 = false;
            boolean teste2 = false;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DA TORRE ATIVADA");
            boolean saida;
            if (((b.lastIndexOf(_c)) > -1) || ((b.lastIndexOf("vazio")) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else {
                System.out.println("teste1=" + teste1);
            }
            if ((((x1 - x) != 0) && ((y1 - y) == 0)) || (((x - x1) == 0) && ((y1 - y) != 0))) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);

            return saida;
        }

        public boolean bispo(int x, int y, int x1, int y1, String b, String c) {
            boolean teste1 = false;
            boolean teste2 = false;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DO BISPO ATIVADA");
            boolean saida;
            if (((b.lastIndexOf("vazio")) > -1) || ((b.lastIndexOf(_c)) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            } else {
                System.out.println("teste1=" + teste1);
            }
            if (((x1 - x) == (y - y1)) || ((x1 - x) == (y1 - y)) || ((x - x1) == (y - y1)) || ((x - x1) == (y1 - y))) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true) && (temPecaNoCaminho(x, y, x1, y1) == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);

            return saida;
        }

        public boolean cavalo(int x, int y, int x1, int y1, String b, String c) {
            boolean teste1 = false;
            boolean teste2 = false;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DO CAVALO ATIVADA");
            boolean saida;
            if ((b.lastIndexOf(_c) > -1) || ((b.lastIndexOf("vazio")) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            }
            if ((((x1 - x) == 2) && ((y - y1) == 1)) || (((x1 - x) == 2) && ((y1 - y) == 1))
                    || (((x - x1) == 2) && ((y - y1) == 1)) || (((x - x1) == 2) && ((y1 - y) == 1))
                    || (((x - x1) == 1) && ((y - y1) == 2)) || (((x - x1) == 1) && ((y1 - y) == 2))
                    || (((x1 - x) == 1) && ((y - y1) == 2)) || (((x1 - x) == 1) && ((y1 - y) == 2))) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);

            return saida;
        }

        public boolean rainha(int x, int y, int x1, int y1, String b, String c) {
            boolean teste1 = false;
            boolean teste2 = false;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DA RAINHA ATIVADA");
            boolean saida;
            if (((b.lastIndexOf("vazio")) > -1) || ((b.lastIndexOf(_c)) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            }
            if ((torre(x, y, x1, y1, b, c) == true) || (bispo(x, y, x1, y1, b, c) == true)) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);

            return saida;
        }

        public boolean rei(int x, int y, int x1, int y1, String b, String c) {
            boolean teste1 = false;
            boolean teste2 = false;
            String _c = new String();
            if (c.lastIndexOf("branco") > -1) {
                _c = "preto";
            } else {
                _c = "branco";
            }
            System.out.println("REGRA DO REI ATIVADA");
            boolean saida;
            if (((b.lastIndexOf("vazio")) > -1) || (b.lastIndexOf(_c) > -1)) {
                teste1 = true;
                System.out.println("teste1=" + teste1);
            }
            if ((((x1 - x) == 1) && ((y - y1) == 1)) || (((x1 - x) == 1) && ((y1 - y) == 1))
                    || (((x - x1) == 1) && ((y - y1) == 1))
                    || (((x - x1) == 1) && ((y1 - y) == 1)) || (((x - x1) == 0) && ((y1 - y) == 1))
                    || (((x - x1) == 0) && ((y - y1) == 1))
                    || (((x - x1) == 1) && ((y1 - y) == 0)) || (((x1 - x) == 1) && ((y - y1) == 0))) {
                teste2 = true;
                System.out.println("teste2=" + teste2);
            } else {
                System.out.println("teste2=" + teste2);
            }
            if ((teste1 == true) && (teste2 == true)) {
                saida = true;
            } else {
                saida = false;
            }
            System.out.println("MOVIMENTO " + saida);

            return saida;
        }

        public boolean temPecaNoCaminho(int a, int b, int c, int d) {
            boolean saida = true;
            String lol = new String("lol");
            System.out.println(lol);
            // CRIAR UMA INST�NCIA PARA VERIFICAR A EXISTENCIA DE PE�AS NO CAMINHO
            return saida;
        }

    }
}
