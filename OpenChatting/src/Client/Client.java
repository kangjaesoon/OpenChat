package Client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Server.Server.RoomInfo;

public class Client extends JFrame implements ActionListener, KeyListener {

   // Login GUI 변수
   private JFrame Login_GUI = new JFrame();
   private JPanel Login_Pane;
   private JTextField ip_tf;
   private JTextField port_tf;
   private JTextField id_tf;
   private JButton login_btn = new JButton("접 속");
   // Main GUI 변수
   private JPanel contentPane;
   private JTextField message_tf;
   private JButton notesend_btn = new JButton("쪽지보내기");
   private JButton joinroom_btn = new JButton("채팅방 참여");
   private JButton quitroom_btn = new JButton("채팅방 나가기");
   private JButton createroom_btn = new JButton("방 만들기");
   private JButton send_btn = new JButton("전 송");
   private JLabel current_roomname = new JLabel("");
   // 배경
   private Image back_img = null;
   // 이미지
   private Image image = null;
   private JButton img_btn;// = new JButton(new ImageIcon(image));
   // 그외 변수들
   Vector user_list = new Vector();
   Vector room_list = new Vector();
   StringTokenizer st;

   private String My_Room; // 내가 있는 방 이름

   private JList User_list = new JList(); // 접속자 리스트
   private JList Room_list = new JList(); // 전체 방목록 리스트

   private JTextArea Chat_area = new JTextArea(); // 채팅창 변수

   // 네트워크를 위한 자원 변수

   private Socket socket;
   private String ip; // 자기자신 ip
   private int port;
   private String id = "";
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;

   Client() { // 생성자 메소드
      Login_init(); // Login창 화면 구성 메소드
      Main_init(); // Main창 화면 구성 메소드
      start();

   }

   private void start() {
      img_btn.addActionListener(this);
      login_btn.addActionListener(this); // 로그인 버튼 리스너
      notesend_btn.addActionListener(this); // 쪽지보내기 버튼 리스너
      joinroom_btn.addActionListener(this); // 채팅방 참여 버튼 리스너
      quitroom_btn.addActionListener(this); // 채팅방 나가기 버튼 리스너
      createroom_btn.addActionListener(this); // 채팅방 만들기 버튼 리스너
      send_btn.addActionListener(this); // 채팅 전송 버튼 리스너
      message_tf.addKeyListener(this);

   }

   private void Main_init() {
      Color color = new Color(136, 192, 225);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 734, 664);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      contentPane.setBackground(color);

      JLabel lbl_user = new JLabel("전 체 접 속 자");
      lbl_user.setBounds(17, 26, 120, 21);
      contentPane.add(lbl_user);

      JScrollPane userlist = new JScrollPane();
      userlist.setBounds(17, 51, 129, 171);
      contentPane.add(userlist);
      userlist.setViewportView(User_list);

      notesend_btn.setBounds(17, 239, 129, 29);
      contentPane.add(notesend_btn);

      JLabel lbl_room = new JLabel("채 팅 방 목 록");
      lbl_room.setBounds(17, 290, 120, 21);
      contentPane.add(lbl_room);

      JScrollPane roomlist = new JScrollPane();
      roomlist.setBounds(17, 314, 129, 171);
      contentPane.add(roomlist);
      roomlist.setViewportView(Room_list);

      joinroom_btn.setBounds(17, 500, 129, 29);
      contentPane.add(joinroom_btn);

      quitroom_btn.setBounds(17, 500, 129, 29);
      contentPane.add(quitroom_btn);
      quitroom_btn.setVisible(false);

      createroom_btn.setBounds(17, 538, 129, 29);
      contentPane.add(createroom_btn);

      JScrollPane chat_area = new JScrollPane();
      chat_area.setBounds(186, 53, 510, 476);
      contentPane.add(chat_area);
      chat_area.setViewportView(Chat_area);

      message_tf = new JTextField();
      message_tf.setBounds(186, 539, 392, 27);
      contentPane.add(message_tf);
      message_tf.setColumns(10);
      message_tf.setEditable(false);

      send_btn.setBounds(585, 538, 107, 29);
      contentPane.add(send_btn);
      send_btn.setEnabled(false);

      current_roomname.setBounds(186, 29, 392, 21);
      contentPane.add(current_roomname);

      Chat_area.setEditable(false);
      this.setResizable(false);
      this.setVisible(false);
   }

   // 배경이미지
   class myPanel extends JPanel {
      public void paint(Graphics g) {
         g.drawImage(back_img, 0, 0, null);
      }
   }

   private void Login_init() {
      // 디자인
      Color color = new Color(136, 192, 225);
      Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Login_GUI.setBounds(100, 100, 330, 491);
      Login_Pane = new JPanel();
      Login_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
      Login_GUI.setContentPane(Login_Pane);
      Login_Pane.setLayout(null);
      Login_Pane.setBackground(color);
      // 로그인 배경이미지
      try {
         URL url = new URL("http://upload.inven.co.kr/upload/2014/04/24/bbs/i4567407501.jpg");
         back_img = ImageIO.read(url);
      } catch (IOException e) {
         e.printStackTrace();
      }

      JLabel lblNewLabel = new JLabel("Server IP");
      lblNewLabel.setBounds(25, 225, 82, 21);
      Login_Pane.add(lblNewLabel);

      JLabel lblNewLabel_1 = new JLabel("Server Port");
      lblNewLabel_1.setBounds(25, 261, 101, 21);
      Login_Pane.add(lblNewLabel_1);

      JLabel lblNewLabel_2 = new JLabel("ID");
      lblNewLabel_2.setBounds(25, 297, 82, 21);
      Login_Pane.add(lblNewLabel_2);

      ip_tf = new JTextField();
      ip_tf.setBounds(131, 225, 166, 27);
      Login_Pane.add(ip_tf);
      ip_tf.setColumns(10);

      port_tf = new JTextField();
      port_tf.setColumns(10);
      port_tf.setBounds(131, 261, 166, 27);
      Login_Pane.add(port_tf);

      id_tf = new JTextField();
      id_tf.setColumns(10);
      id_tf.setBounds(131, 297, 166, 27);
      Login_Pane.add(id_tf);

      login_btn.setBounds(17, 355, 280, 29);
      Login_Pane.add(login_btn);

      // 로그인 상단이미지
      try {
         URL url = new URL(
               "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTkztbROUbC00NOsscfDBiGbuPQx4Q8dKIowBt2uiEnj0WXLEnw");
         image = ImageIO.read(url);
         ImageIcon originIcon = new ImageIcon(image);
         Image originImg = originIcon.getImage();
         Image changedImg = originImg.getScaledInstance(330, 210, Image.SCALE_SMOOTH);
         ImageIcon Icon = new ImageIcon(changedImg);
         img_btn = new JButton(new ImageIcon(changedImg));
      } catch (IOException e) {
         e.printStackTrace();
      }
      img_btn.setBounds(33, 30, 250, 140);
      Login_Pane.add(img_btn);

      Login_GUI.setVisible(true); // 화면에 보인다
      Login_GUI.setResizable(false);
   }

   private void Network() {

      try {
         socket = new Socket(ip, port);

         if (socket != null) { // 정상적으로 소켓이 연결되었을경우
            Connection();
         }

      } catch (UnknownHostException e) {
         JOptionPane.showMessageDialog(null, "연결 실패", "알림", JOptionPane.ERROR_MESSAGE);
      } catch (IOException e) {
         JOptionPane.showMessageDialog(null, "연결 실패", "알림", JOptionPane.ERROR_MESSAGE);
      }

   }

   private void Connection() { // 실제적인 메소드 연결부분

      try {
         is = socket.getInputStream();
         dis = new DataInputStream(is);

         os = socket.getOutputStream();
         dos = new DataOutputStream(os);
      } catch (IOException e) { // 에러처리
         JOptionPane.showMessageDialog(null, "연결 실패", "알림", JOptionPane.ERROR_MESSAGE);
      } // Stream 설정 끝

      this.setVisible(true);
      this.Login_GUI.setVisible(false);

      // 처음 접속시 ID 전송
      send_message(id);

      // User_list 에 사용자 추가
      user_list.add(id);

      Thread th = new Thread(new Runnable() {

         @Override
         public void run() {

            while (true) {

               try {
                  String msg = dis.readUTF(); // 메세지 수신

                  System.out.println("서버로부터 수신된 메세지 : " + msg);

                  inmessage(msg);

               } catch (IOException e) {

                  try {
                     os.close();
                     is.close();
                     dos.close();
                     dis.close();
                     socket.close();
                     JOptionPane.showMessageDialog(null, "서버와 접속 끊어짐", "알림", JOptionPane.ERROR_MESSAGE);
                  } catch (IOException e1) {

                  }
                  break;
               }
            }
         }
      });

      th.start();

   }

   private void inmessage(String str) { // 서버로부터 들어오는 모든 메세지
      st = new StringTokenizer(str, "/");

      String protocol = st.nextToken();
      String Message = st.nextToken();

      System.out.println("프로토콜 : " + protocol);
      System.out.println("내용: " + Message);

      if (protocol.equals("NewUser")) { // 새로운 접속자
         user_list.add(Message);
      } else if (protocol.equals("OldUser")) {
         user_list.add(Message);
      } else if (protocol.contentEquals("Note")) {
         // st = new StringTokenizer(Message,"/");
         String note = st.nextToken();
         System.out.println(Message + "사용자로부터 온 쪽지 " + note);
         JOptionPane.showMessageDialog(null, note, Message + "님으로 부터 쪽지", JOptionPane.CLOSED_OPTION);
      } else if (protocol.equals("user_list_update")) {
         // User_list.updateUI();
         User_list.setListData(user_list);
      } else if (protocol.equals("CreateRoom")) { // 방을 만들었을 때
         My_Room = Message;
         message_tf.setEditable(true);
         send_btn.setEnabled(true);
         joinroom_btn.setVisible(false);
         createroom_btn.setEnabled(false);
         quitroom_btn.setVisible(true);
         current_roomname.setText("현재 접속 중인 방 : " + Message); 
      } else if (protocol.equals("CreateRoomFail")) { // 방만들기 실패 했을 때
         JOptionPane.showMessageDialog(null, "같은 이름의 방이 존재합니다. 다른 이름으로 만들어 주세요", "알림", JOptionPane.ERROR_MESSAGE);
      } else if (protocol.equals("New_Room")) { // 새로운 방을 만들었을 때
         room_list.add(Message);
         Room_list.setListData(room_list);
      } else if (protocol.equals("Chatting")) {
         String msg = st.nextToken();
         Chat_area.append(Message + " : " + msg + "\n");
         Chat_area.setCaretPosition(Chat_area.getDocument().getLength());
      } else if (protocol.equals("OldRoom")) {
         room_list.add(Message);
      } else if (protocol.equals("room_list_update")) {
         Room_list.setListData(room_list);
      } else if (protocol.equals("JoinRoom")) {
         My_Room = Message;
         message_tf.setEditable(true);
         send_btn.setEnabled(true);
         joinroom_btn.setVisible(false);
         createroom_btn.setEnabled(false);
         Chat_area.setText("");
         current_roomname.setText("현재 접속 중인 방 : " + Message); 
      } else if (protocol.equals("User_out")) {
         user_list.remove(Message);
      } else if (protocol.equals("RUcnt")) {
         JOptionPane.showMessageDialog(null, Message, "알림", JOptionPane.INFORMATION_MESSAGE);
      } else if (protocol.equals("QuitRoom")) {
         message_tf.setEditable(false);
         send_btn.setEnabled(false);
         joinroom_btn.setVisible(true);
         Chat_area.setText("");
         Chat_area.append("");
         current_roomname.setText(""); 
         JOptionPane.showMessageDialog(null, "방을 나갔습니다", "알림", JOptionPane.INFORMATION_MESSAGE);
         createroom_btn.setEnabled(true);
      } else if (protocol.equals("DeleteRoom")) {
         room_list.remove(Message);
         Room_list.setListData(room_list);
      }
   }

   private void send_message(String str) { // 서버에게 메세지를 보내는 부분
      try {
         dos.writeUTF(str);
      } catch (IOException e) { // 에러 처리
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {

      new Client();

   }

   @Override
   public void actionPerformed(ActionEvent e) {
      // login_btn = 로그인버튼
      if (e.getSource() == login_btn) {
         System.out.println("로그인 버튼 클릭");
         if (ip_tf.getText().length() == 0) {
            ip_tf.setText("IP를 입력해주세요");
            ip_tf.requestFocus();
         } else if (port_tf.getText().length() == 0) {
            port_tf.setText("Port번호를 입력해주세요");
            port_tf.requestFocus();
         } else if (id_tf.getText().length() == 0) {
            id_tf.setText("ID를 입력해주세요");
            id_tf.requestFocus();
         } else {
            ip = ip_tf.getText().trim(); // ip를 받아오는 부분
            port = Integer.parseInt(port_tf.getText().trim()); // port int형으로 받아오는 부분
            id = id_tf.getText().trim(); // id를 받아오는 부분
            Network();
         }
      } else if (e.getSource() == img_btn) {
         JOptionPane.showMessageDialog(null, "개발자 : 강재순, 최우석 ", "오픈채팅_v1.0.0", JOptionPane.CLOSED_OPTION);
      } else if (e.getSource() == notesend_btn) {
         System.out.println("쪽지 보내기 버튼 클릭");
         if ((String) User_list.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(null, "쪽지를 보낼 사용자를 선택해주세요. ", "알림", JOptionPane.ERROR_MESSAGE);
         } else {
            String user = (String) User_list.getSelectedValue();
            if (!user.equals("")) {
               String note = JOptionPane.showInputDialog(User_list.getSelectedValue() + "에게 보낼 메세지");

               if (note != null) {
                  send_message("Note/" + user + "/" + note);
                  // ex) Note/User2/나는 User1
               }
               System.out.println("받는 사람 : " + user + " | 보낼 내용 : " + note);

            }
         }
      } else if (e.getSource() == joinroom_btn)

      {
         if ((String) Room_list.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(null, "입장할 방을 선택해주세요", "알림", JOptionPane.ERROR_MESSAGE);
         } else {
            String JoinRoom = (String) Room_list.getSelectedValue();
            send_message("JoinRoom/" + JoinRoom);
            send_message("RUcnt/" + JoinRoom);
            joinroom_btn.setVisible(false);
            quitroom_btn.setVisible(true);
            System.out.println("방 참여 버튼 클릭");
         }
      } else if (e.getSource() == quitroom_btn) {
         String QuitRoom = "방나가기";
         // String DeleteRoom = (String) Room_list.getSelectedValue();
         joinroom_btn.setVisible(true);
         createroom_btn.setEnabled(true);
         quitroom_btn.setVisible(false);
         send_message("QuitRoom/" + QuitRoom);
         send_message("DeleteRoom/" + My_Room);
      } else if (e.getSource() == createroom_btn) {
         String roomname = JOptionPane.showInputDialog("방 이름");
         if (roomname != null) {
            send_message("CreateRoom/" + roomname);
         }
         System.out.println("방 만들기 버튼 클릭");
      } else if (e.getSource() == send_btn) {
         if (message_tf.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "공백은 전송이 안됩니다!", "알림", JOptionPane.ERROR_MESSAGE);
         } else {
            send_message("Chatting/" + My_Room + "/" + message_tf.getText().trim());
            message_tf.setText("");
            message_tf.requestFocus();
            // Chatting + 방이름 + 내용
            System.out.println("채팅 전송 버튼 클릭");
         }
      }

   }

   @Override
   public void keyTyped(KeyEvent e) {
      // TODO Auto-generated method stub

   }

   @Override
   public void keyPressed(KeyEvent e) {
      if (!message_tf.getText().equals("")) {
         if (e.getKeyCode() == 10) {
            send_message("Chatting/" + My_Room + "/" + message_tf.getText().trim());
            message_tf.setText("");
            message_tf.requestFocus();
         }
      }

   }

   @Override
   public void keyReleased(KeyEvent e) {
      // TODO Auto-generated method stub

   }

}