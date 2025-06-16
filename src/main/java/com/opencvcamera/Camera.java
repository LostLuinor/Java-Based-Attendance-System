package com.opencvcamera;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class Camera extends JFrame {
	
	// all camera stuff here
	Session newSession = null;
	MimeMessage mimeMessage = null;
	String name = null;
	String rollN = null;
	ArrayList<String> studentDetails = new ArrayList<String>(2);
	HashMap<Integer,ArrayList<String>> studentData = new HashMap<Integer,ArrayList<String>>();
	private JLabel cameraScreen;
	private JPanel contentPane;
	private JButton btnCapture;
	private JButton btnAttendance;
	private JButton btnsendAttendance;
	private VideoCapture capture;
	private Mat image;
	private boolean clicked = false;
	private boolean clicked1 = false;
	private boolean clicked2 = false;
	
	public Camera() {
		// buttons and camera locations for popup box
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 933, 622);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		cameraScreen = new JLabel();
		cameraScreen.setBounds(10, 11, 633, 563);
		contentPane.add(cameraScreen);
		
		btnCapture = new JButton("Register Student");
		btnCapture.setBounds(714, 47, 148, 69);
		contentPane.add(btnCapture);
		
		btnAttendance = new JButton("Take Attendance");
		btnAttendance.setBounds(714, 258, 148, 69);
		contentPane.add(btnAttendance);
		
		btnsendAttendance = new JButton("Send Attendance");
		btnsendAttendance.setBounds(714, 460, 148, 69);
		contentPane.add(btnsendAttendance);

		
		btnCapture.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!clicked1) {
					name = null;
					clicked = true;
				}
				
			}
		});
		
		btnAttendance.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
					if(clicked1) {
                        name = null;
                        clicked1 = false;
                    } else {
                        	clicked1 = true;
                    }
					
			}
		});
		
		btnsendAttendance.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clicked2 = true;
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				capture.release();
				image.release();
				System.exit(0);
			}
		});
		// the popup box where camera lies lmao
		setVisible(true);
	}
	
	public void startCamera(){
		Mat capimage = null;
		capture = new VideoCapture(0); //starts the camera
		image  = new Mat();
		ImageIcon icon;

		String idName;
		String idRollN;
		int id = 0;
		int count = 0;
		
		//finds out how many students have been registered so far and initializes the existing students as absent
		String attendance = "data/studentData.csv";
		try {
			Scanner lb = new Scanner(new File(attendance));
			lb.useDelimiter(",|\n");
			lb.next();
			lb.next();
			lb.next();
			while(lb.hasNext()) {
				id = lb.nextInt();
				idRollN = lb.next();
				idRollN = idRollN.substring(0, idRollN.length()-1);
				idName = lb.next();
				idName = idName.substring(0, idName.length()-1);
				
				ArrayList<String> studentDetails = new ArrayList<String>(4);
				studentDetails.add(idRollN);
				studentDetails.add(idName);
				studentDetails.add("Absent");
				studentDetails.add("Absent");
				studentData.put(id, new ArrayList<String>(studentDetails));
			}
			lb.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(id!=0) {
			id++;
		}
		
		while(true) {
			capture.read(image);
			
			
			
			final BytePointer buf = new BytePointer();
			RectVector faces = new RectVector();
			
			Mat grayFrame = new Mat();
			opencv_imgproc.cvtColor(image, grayFrame, opencv_imgproc.COLOR_BGR2GRAY);
			
			opencv_imgproc.equalizeHist(grayFrame, grayFrame);
			
			int height = grayFrame.arrayHeight();
			int absoluteFaceSize = 0;
			if(Math.round(height*0.2f)>0) {
				absoluteFaceSize = Math.round(height*0.2f);
			}
			
			CascadeClassifier faceCascade = new CascadeClassifier();
			
			//detects face and draws a rectangle
			faceCascade.load("data/haarcascade_frontalface_alt2.xml");
			faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0|opencv_objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize,absoluteFaceSize), new Size());
			
			Rect[] faceArray = new Rect[(int) faces.size()];
			for (int i = 0; i < faces.size(); i++) {
			    faceArray[i] = faces.get(i);
			}
			for(int i = 0; i < faceArray.length; i++) {
				
				opencv_imgproc.rectangle(image, faceArray[i], new Scalar(0,0, 255, 3));
				capimage = new Mat(image,faceArray[i]);
			}
			
			//code for registering student
			if(clicked) {
				if(name == null) {
					name = JOptionPane.showInputDialog(this,"Enter Image Name");
					rollN = JOptionPane.showInputDialog(this,"Enter Image Roll Number");
					if(name==null) {
						name = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss").format(new Date());
					}
					
				}
				opencv_imgcodecs.imwrite("images/" + id + "-" + name + "_" + count +".jpg",capimage);
				count++;
				if(count<50) {
					clicked = true;
				}else {
					System.out.println("Training model...");
					MatVector images = new MatVector(50);
					Mat pics = new Mat(50, 1, opencv_core.CV_32SC1);
					IntBuffer picsBuf = pics.createBuffer();

					// Read face images and set labels
					for (int i = 0; i < 50; i++) {
					    String filename = "images/"+ id + "-" + name + "_" + i +".jpg";
					    Mat img = opencv_imgcodecs.imread(filename, IMREAD_GRAYSCALE);
					    images.put(i, img);
					    picsBuf.put(i, id);
					}       

					// Create the face recognizer object
					File dataFile = new File("data/lbph_model.yml");
					FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
					// if the yml file exists it adds the new data after it
					if(dataFile.exists()) {
						faceRecognizer.read("data/lbph_model.yml");
						faceRecognizer.update(images, pics);
						faceRecognizer.save("data/lbph_model.yml");
					}else {
						faceRecognizer.train(images,pics);
						faceRecognizer.save("data/lbph_model.yml");
					}
					System.out.println("Model trained and saved");
					//stores the student data in a csv file
					try {
						PrintWriter writer = new PrintWriter(new FileWriter(attendance,true));
						writer.println(id + "," + rollN + "," + name);
						writer.flush();
						writer.close();
					} catch (IOException e) {
						System.out.println("Error writing to file");
						e.printStackTrace();
					}

					ArrayList<String> studentDetails = new ArrayList<String>(4);
					studentDetails.add(rollN);
					studentDetails.add(name);
					studentDetails.add("Absent");
					studentDetails.add("Absent");
					studentData.put(id, new ArrayList<String>(studentDetails));
					id++;
					JOptionPane.showMessageDialog(this, "Student Registered Successfully");
					
					name = null;
					count = 0;
					clicked = false;
				}
			}
			
			if(clicked1) {
				Rect rect1;
				for (int i = 0; i < faces.size(); i++) {
		            rect1 = faces.get(i);
		            opencv_imgproc.rectangle(image, rect1, new Scalar(0, 0, 0, 0));
		            
		            capimage = new Mat(image, rect1);
		            opencv_imgproc.cvtColor(capimage, capimage, opencv_imgproc.COLOR_BGRA2GRAY);            
		            FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
		            faceRecognizer.read("data/lbph_model.yml");
		            faceRecognizer.setThreshold(80);
		            int faceLabel = faceRecognizer.predict_label(capimage);

		            if (faceLabel==-1) {
		            	name = "not recognized";
		            } else {
		            	opencv_imgproc.rectangle(image, rect1, new Scalar(0, 0, 255, 0));
		                ArrayList<String> recStudent = studentData.get(faceLabel);
		                name = recStudent.get(1);
		                recStudent.add(2,"Present");
		                recStudent.add(3, new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss").format(new Date()));
		                studentData.put(faceLabel,recStudent);
		            }
		            

		            int x = Math.max(rect1.tl().x() - 10, 0);

		            int y = Math.max(rect1.tl().y() - 10, 0);
		            opencv_imgproc.putText(image, name, new Point(x, y), opencv_imgproc.FONT_HERSHEY_PLAIN, 1.7, new Scalar(0, 255, 0, 2));

		        }
			}
			
			if(clicked2) {
				try {
					PrintWriter writer = new PrintWriter(new FileWriter("data/attendanceSheet.csv"));
					writer.println("ID,Roll No.,Name,Status,Date And Time");
					System.out.println(studentData.size());
					for (int i = 0; i < studentData.size(); i++) {
						ArrayList<String> data = studentData.get(i);
						writer.println(i + "," + data.get(0) + "," + data.get(1) + "," + data.get(2) + "," + data.get(3));
					}
					writer.flush();
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				setupServerProperties();
				try {
					draftEmail();
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					sendEmail();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					PrintWriter writer = new PrintWriter(new FileWriter("data/attendanceSheet.csv"));
					writer.println(",,,");
					for (int key : studentData.keySet()) {
						ArrayList<String> data = studentData.get(key);
						data.add(3,"Absent");
						data.add(4,"Absent");
						writer.println(",");
					}
					writer.flush();
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog(this, "Attendance Sent Successfully");
				clicked2 = false;
			}
			
			opencv_imgcodecs.imencode(".jpg",image,buf);
			
			Image im = null;
			try {
				im = ImageIO.read(new ByteArrayInputStream(buf.getStringBytes()));
			}catch(Exception e) {
				e.printStackTrace();
			}
			BufferedImage bimage = (BufferedImage) im;
			
			icon = new ImageIcon(bimage);
			cameraScreen.setIcon(icon);
			faceCascade.close();
		}			
		}
	
	private void setupServerProperties() {
		Properties properties = System.getProperties();
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		newSession = Session.getDefaultInstance(properties,null);
	}
	
	private MimeMessage draftEmail() throws AddressException, MessagingException, IOException {
		String[] emailReceipients = {"REDACTED"};
		String emailSubject = "Attendance Report";
		String emailBody = "Please find the attached attendance sheet for today's class.";
		mimeMessage = new MimeMessage(newSession);
		
		for (int i =0 ;i<emailReceipients.length;i++)
		{
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailReceipients[i]));
		}
		mimeMessage.setSubject(emailSubject);
	    
	    
		 MimeBodyPart bodyPart = new MimeBodyPart();
		 bodyPart.setContent(emailBody,"html/text");
		 
		 MimeBodyPart attachmentBodyPart = new MimeBodyPart();
	   attachmentBodyPart.attachFile(new File("data/attendanceSheet.csv"));
		 
		 MimeMultipart multiPart = new MimeMultipart();
		 multiPart.addBodyPart(bodyPart);
		 multiPart.addBodyPart(attachmentBodyPart);
		 mimeMessage.setContent(multiPart);
		 return mimeMessage;
	}
	
	private void sendEmail() throws MessagingException {
		String fromUser = "REDACTED";
		String fromUserPassword = "REDACTED";
		String emailHost = "smtp.gmail.com";
		Transport transport = newSession.getTransport("smtp");
		transport.connect(emailHost, fromUser, fromUserPassword);
		transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		transport.close();
		System.out.println("Email successfully sent!!!");
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Camera camera = new Camera();
				new Thread(new Runnable() {
					@Override
					public void run() {
						camera.startCamera();
						
					}
				}).start();
			}
		});
		
		
		
	}

}
