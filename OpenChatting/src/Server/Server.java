package Server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

   private JPanel contentPane;
   private JTextField port_tf;
   private JTextArea textArea = new JTextArea();
   private JButton start_btn = new JButton("���� ����");
   private JButton stop_btn = new JButton("���� ����");
   // Network �ڿ�
   private ServerSocket server_socket;
   private Socket socket;
   private int port;
   private Vector user_vc = new Vector();
   private Vector room_vc = new Vector();

   private StringTokenizer st;

   Server() {// ������
      init(); // ȭ�� ���� �޼ҵ�
      start(); // ������ ���� �޼ҵ�
   }

   private void start() {
      start_btn.addActionListener(this);
      stop_btn.addActionListener(this);
   }

   // UI
   public void init() {
      Color color = new Color(136, 192, 225);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 375, 502);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      contentPane.setBackground(color);

      JLabel lblNewLabel = new JLabel("��Ʈ��ȣ : ");
      lblNewLabel.setBounds(27, 332, 102, 21);
      contentPane.add(lblNewLabel);

      port_tf = new JTextField();
      port_tf.setBounds(121, 329, 230, 27);
      contentPane.add(port_tf);
      port_tf.setColumns(10);

      start_btn.setBounds(17, 389, 166, 29);
      contentPane.add(start_btn);

      stop_btn.setBounds(183, 389, 168, 29);
      contentPane.add(stop_btn);
      stop_btn.setEnabled(false);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(17, 15, 332, 299);
      contentPane.add(scrollPane);

      scrollPane.setViewportView(textArea);
      textArea.setEditable(false);

      this.setResizable(false);
      this.setVisible(true);
   }

   private void Server_start() {
      try {
         server_socket = new ServerSocket(port);
      } catch (IOException e) {
         JOptionPane.showMessageDialog(null, "�̹� ������� ��Ʈ�Դϴ�", "�˸�", JOptionPane.ERROR_MESSAGE);
      }
      if (server_socket != null) { // ���������� ��Ʈ�� ������ ���
         Connection();
      }
   }

   private void Connection() {
      Thread th = new Thread(new Runnable() { // 1���� �����忡���� 1������ �ϸ� ó���� �� �ִ�.
         @Override
         public void run() { // �����忡�� ó���� ���� �����Ѵ�.
            while (true) {
               try {
                  textArea.append("����� ���� �����\n");
                  socket = server_socket.accept(); // ����� ���� ���Ѵ��
                  textArea.append("����� ����!!!!\n");
                  textArea.setCaretPosition(textArea.getDocument().getLength());
                  UserInfo user = new UserInfo(socket);
                  user.start(); // ��ü�� ������ ����
               } catch (IOException e) {
                  break;
               }
            }
         }
      });
      th.start();
   }

   public static void main(String[] args) {

      new Server();

   }

   @Override
   public void actionPerformed(ActionEvent e) {

      if (e.getSource() == start_btn) {
         System.out.println("���� ��ŸƮ ��ư Ŭ��");
         port = Integer.parseInt(port_tf.getText().trim());

         Server_start(); // ���� ���� �� ����� ���� ���

         start_btn.setEnabled(false);
         port_tf.setEnabled(false);
         stop_btn.setEnabled(true);
      } else if (e.getSource() == stop_btn) {
         System.out.println("���� ��ž ��ư Ŭ��");
         start_btn.setEnabled(true);
         port_tf.setEnabled(true);
         stop_btn.setEnabled(false);
         try {
            server_socket.close();
            user_vc.removeAllElements();
            room_vc.removeAllElements();
         } catch (IOException e1) {

         }
      }

   } // �׼� �̺�Ʈ ��

   class UserInfo extends Thread {
      private OutputStream os;
      private InputStream is;
      private DataOutputStream dos;
      private DataInputStream dis;
      private Socket user_socket;
      private String Nickname = "";
      private boolean RoomCh = true;

      UserInfo(Socket soc) { // ������ �޼ҵ�
         this.user_socket = soc;
         UserNetwork();
      }

      private void UserNetwork() { // ��Ʈ��ũ �ڿ� ����
         try {
            is = user_socket.getInputStream();
            dis = new DataInputStream(is);
            os = user_socket.getOutputStream();
            dos = new DataOutputStream(os);
            Nickname = dis.readUTF(); // ������� �г����� �޴´�.
            textArea.append(Nickname + " : ����� ����!");
            BroadCast("NewUser/" + Nickname); // ���� ����ڿ��� �ڽ��� �˸�
            // �ڽſ��� ���� ����ڸ� �޾ƿ��� �κ�
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               send_Message("OldUser/" + u.Nickname);
            }
            // �ڽſ��� ���� �� ����� �޾ƿ��� �κ�
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               send_Message("OldRoom/" + r.Room_name);
            }
            send_Message("room_list_update/ ");
            user_vc.add(this); // ����ڿ��� �˸� �� Vector�� �ڽ��� �߰�

            BroadCast("user_list_update/ ");

         } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Stream���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
         }
      }

      public void run() { // Thread���� ó���� ����

         while (true) {
            try {
               String msg = dis.readUTF(); // �޼��� ����
               textArea.append(Nickname + " : ����ڷκ��� ���� �޼��� :" + msg + "\n");
               textArea.setCaretPosition(textArea.getDocument().getLength());
               InMessage(msg);
            } catch (IOException e) {
               textArea.append(Nickname + ": ����ڰ� ������ �������ϴ�.\n");
               textArea.setCaretPosition(textArea.getDocument().getLength());
               
               try {
                  dos.close();
                  dis.close();
                  user_socket.close();
                  user_vc.remove(this);
                  // ���濡�� ����� ����
                  for (int i = 0; i < room_vc.size(); i++) {
                     RoomInfo r = (RoomInfo) room_vc.elementAt(i);
                     r.remove_User(this);
                  }
                  BroadCast("User_out/" + Nickname);
                  BroadCast("user_list_update/"+"������");
               } catch (IOException e1) {
               }
               break;
            }

         }
      } // run �޼ҵ� ��

      private void InMessage(String str) { // Ŭ���̾�Ʈ�� ���� ������ �޼��� ó��
         st = new StringTokenizer(str, "/");
         String protocol = st.nextToken();
         String message = st.nextToken();
         if (protocol.equals("Note")) {
            String note = st.nextToken();
            // ���Ϳ��� �ش� ����ڸ� ã�Ƽ� �޼��� ����
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               if (u.Nickname.equals(message)) {
                  u.send_Message("Note/" + Nickname + "/" + note);
               }
            }
         } // if�� ��
         else if (protocol.equals("CreateRoom")) {
            // 1. ���� ���� ���� �����ϴ��� Ȯ���Ѵ�.
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) { // ������� �ϴ� ���� �̹� ���� �� ��
                  send_Message("CreateRoomFail/ok");
                  RoomCh = false;
                  break;
               }
            } // for�� ��
            if (RoomCh) { // ���� ���� �� ���� ��
               RoomInfo new_room = new RoomInfo(message, this);
               room_vc.add(new_room); // ��ü �� ���Ϳ� ���� �߰�
               send_Message("CreateRoom/" + message);
               BroadCast("New_Room/" + message);
            }
            RoomCh = true;
         } // else if �� ��
         else if (protocol.equals("Chatting")) {
            String msg = st.nextToken();
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) { // �ش� ���� ã���� ��
                  r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
               }
            }
         } // else if ��
         else if (protocol.equals("JoinRoom")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) {
                  // ���ο� ����ڸ� �˸���
                  r.BroadCast_Room("Chatting/�˸�/******" + Nickname + "���� ���� �ϼ̽��ϴ�******");
                  // ����� �߰�
                  r.Add_User(this);
                  send_Message("JoinRoom/" + message);
               }
            }
         } else if (protocol.equals("RUcnt")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) {
                  send_Message("RUcnt/" + "����" + message + "�濡 " + r.RU_Cnt() + "�� �ֽ��ϴ�.");
               }
            }
         } else if (protocol.equals("QuitRoom")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               r.remove_User(this);
            }
            send_Message("QuitRoom/" + message);
            
         } else if (protocol.equals("DeleteRoom")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) {
                  if (r.RU_Cnt() == 0) {
                     room_vc.remove(i);
                     BroadCast("DeleteRoom/"+message);
                  }
               }
            }
         }
      }

      private void BroadCast(String str) { // ��ü ����ڿ��� �޼��� ������ �κ�
         for (int i = 0; i < user_vc.size(); i++) { // ���� ���ӵ� ����ڿ��� ���ο� ����� �˸�
            UserInfo u = (UserInfo) user_vc.elementAt(i);
            u.send_Message(str);
         }
      }

      private void send_Message(String str) { // ���ڿ��� �޾Ƽ� ����
         try {
            dos.writeUTF(str);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   } // UserInfo class ��

   public class RoomInfo {
      private String Room_name;
      private Vector Room_user_vc = new Vector();
      private Integer Rucnt;

      RoomInfo(String str, UserInfo u) {
         this.Room_name = str;
         this.Room_user_vc.add(u);
      }

      public void BroadCast_Room(String str) { // ���� ���� ��� ������� �˸���
         for (int i = 0; i < Room_user_vc.size(); i++) {
            UserInfo u = (UserInfo) Room_user_vc.elementAt(i);
            u.send_Message(str);
         }
      }

      private void Add_User(UserInfo u) {
         this.Room_user_vc.add(u);
      }

      private void remove_User(UserInfo u) {
         this.Room_user_vc.remove(u);
      }

      private Integer RU_Cnt() {
         Rucnt = Room_user_vc.size();
         return Rucnt;
      }

   }
}