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
   private JButton start_btn = new JButton("서버 실행");
   private JButton stop_btn = new JButton("서버 중지");
   // Network 자원
   private ServerSocket server_socket;
   private Socket socket;
   private int port;
   private Vector user_vc = new Vector();
   private Vector room_vc = new Vector();

   private StringTokenizer st;

   Server() {// 생성자
      init(); // 화면 생성 메소드
      start(); // 리스너 설정 메소드
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

      JLabel lblNewLabel = new JLabel("포트번호 : ");
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
         JOptionPane.showMessageDialog(null, "이미 사용중인 포트입니다", "알림", JOptionPane.ERROR_MESSAGE);
      }
      if (server_socket != null) { // 정상적으로 포트가 열렸을 경우
         Connection();
      }
   }

   private void Connection() {
      Thread th = new Thread(new Runnable() { // 1가지 스레드에서는 1가지의 일만 처리할 수 있다.
         @Override
         public void run() { // 스레드에서 처리할 일을 기재한다.
            while (true) {
               try {
                  textArea.append("사용자 접속 대기중\n");
                  socket = server_socket.accept(); // 사용자 접속 무한대기
                  textArea.append("사용자 접속!!!!\n");
                  textArea.setCaretPosition(textArea.getDocument().getLength());
                  UserInfo user = new UserInfo(socket);
                  user.start(); // 객체의 스레드 실행
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
         System.out.println("서버 스타트 버튼 클릭");
         port = Integer.parseInt(port_tf.getText().trim());

         Server_start(); // 소켓 생성 및 사용자 접속 대기

         start_btn.setEnabled(false);
         port_tf.setEnabled(false);
         stop_btn.setEnabled(true);
      } else if (e.getSource() == stop_btn) {
         System.out.println("서버 스탑 버튼 클릭");
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

   } // 액션 이벤트 끝

   class UserInfo extends Thread {
      private OutputStream os;
      private InputStream is;
      private DataOutputStream dos;
      private DataInputStream dis;
      private Socket user_socket;
      private String Nickname = "";
      private boolean RoomCh = true;

      UserInfo(Socket soc) { // 생성자 메소드
         this.user_socket = soc;
         UserNetwork();
      }

      private void UserNetwork() { // 네트워크 자원 설정
         try {
            is = user_socket.getInputStream();
            dis = new DataInputStream(is);
            os = user_socket.getOutputStream();
            dos = new DataOutputStream(os);
            Nickname = dis.readUTF(); // 사용자의 닉네임을 받는다.
            textArea.append(Nickname + " : 사용자 접속!");
            BroadCast("NewUser/" + Nickname); // 기존 사용자에게 자신을 알림
            // 자신에게 기존 사용자를 받아오는 부분
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               send_Message("OldUser/" + u.Nickname);
            }
            // 자신에게 기존 방 목록을 받아오는 부분
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               send_Message("OldRoom/" + r.Room_name);
            }
            send_Message("room_list_update/ ");
            user_vc.add(this); // 사용자에게 알린 후 Vector에 자신을 추가

            BroadCast("user_list_update/ ");

         } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Stream설정 에러", "알림", JOptionPane.ERROR_MESSAGE);
         }
      }

      public void run() { // Thread에서 처리할 내용

         while (true) {
            try {
               String msg = dis.readUTF(); // 메세지 수신
               textArea.append(Nickname + " : 사용자로부터 들어온 메세지 :" + msg + "\n");
               textArea.setCaretPosition(textArea.getDocument().getLength());
               InMessage(msg);
            } catch (IOException e) {
               textArea.append(Nickname + ": 사용자가 접속을 끊었습니다.\n");
               textArea.setCaretPosition(textArea.getDocument().getLength());
               
               try {
                  dos.close();
                  dis.close();
                  user_socket.close();
                  user_vc.remove(this);
                  // 모든방에서 사용자 제거
                  for (int i = 0; i < room_vc.size(); i++) {
                     RoomInfo r = (RoomInfo) room_vc.elementAt(i);
                     r.remove_User(this);
                  }
                  BroadCast("User_out/" + Nickname);
                  BroadCast("user_list_update/"+"나감ㅠ");
               } catch (IOException e1) {
               }
               break;
            }

         }
      } // run 메소드 끝

      private void InMessage(String str) { // 클라이언트로 부터 들어오는 메세지 처리
         st = new StringTokenizer(str, "/");
         String protocol = st.nextToken();
         String message = st.nextToken();
         if (protocol.equals("Note")) {
            String note = st.nextToken();
            // 벡터에서 해당 사용자를 찾아서 메세지 전송
            for (int i = 0; i < user_vc.size(); i++) {
               UserInfo u = (UserInfo) user_vc.elementAt(i);
               if (u.Nickname.equals(message)) {
                  u.send_Message("Note/" + Nickname + "/" + note);
               }
            }
         } // if문 끝
         else if (protocol.equals("CreateRoom")) {
            // 1. 현재 같은 방이 존재하는지 확인한다.
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) { // 만들고자 하는 방이 이미 존재 할 때
                  send_Message("CreateRoomFail/ok");
                  RoomCh = false;
                  break;
               }
            } // for문 끝
            if (RoomCh) { // 방을 만들 수 있을 때
               RoomInfo new_room = new RoomInfo(message, this);
               room_vc.add(new_room); // 전체 방 벡터에 방을 추가
               send_Message("CreateRoom/" + message);
               BroadCast("New_Room/" + message);
            }
            RoomCh = true;
         } // else if 문 끝
         else if (protocol.equals("Chatting")) {
            String msg = st.nextToken();
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) { // 해당 방을 찾았을 때
                  r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
               }
            }
         } // else if 끝
         else if (protocol.equals("JoinRoom")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) {
                  // 새로운 사용자를 알린다
                  r.BroadCast_Room("Chatting/알림/******" + Nickname + "님이 입장 하셨습니다******");
                  // 사용자 추가
                  r.Add_User(this);
                  send_Message("JoinRoom/" + message);
               }
            }
         } else if (protocol.equals("RUcnt")) {
            for (int i = 0; i < room_vc.size(); i++) {
               RoomInfo r = (RoomInfo) room_vc.elementAt(i);
               if (r.Room_name.equals(message)) {
                  send_Message("RUcnt/" + "현재" + message + "방에 " + r.RU_Cnt() + "명 있습니다.");
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

      private void BroadCast(String str) { // 전체 사용자에게 메세지 보내는 부분
         for (int i = 0; i < user_vc.size(); i++) { // 현재 접속된 사용자에게 새로운 사용자 알림
            UserInfo u = (UserInfo) user_vc.elementAt(i);
            u.send_Message(str);
         }
      }

      private void send_Message(String str) { // 문자열을 받아서 전송
         try {
            dos.writeUTF(str);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   } // UserInfo class 끝

   public class RoomInfo {
      private String Room_name;
      private Vector Room_user_vc = new Vector();
      private Integer Rucnt;

      RoomInfo(String str, UserInfo u) {
         this.Room_name = str;
         this.Room_user_vc.add(u);
      }

      public void BroadCast_Room(String str) { // 현재 방의 모든 사람에게 알린다
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