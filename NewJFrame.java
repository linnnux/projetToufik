/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javaapplication.NewJFrame.kmeans.MODE_CONTINUOUS;
import static javaapplication.NewJFrame.kmeans.MODE_ITERATIVE;

import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author laptop
 */
public class NewJFrame extends javax.swing.JFrame {
    public class kmeans { 
    BufferedImage original; 
    BufferedImage result; 
    Cluster[] clusters; 
    public static final int MODE_CONTINUOUS = 1; 
    public static final int MODE_ITERATIVE = 2; 
         
    public kmeans() {    } 
     
    public BufferedImage calculate(BufferedImage image,  
                                            int k, int mode) { 
        long start = System.currentTimeMillis(); 
        int w = image.getWidth(); 
        int h = image.getHeight(); 
        // create clusters 
        clusters = createClusters(image,k); 
        // create cluster lookup table 
        int[] lut = new int[w*h]; 
        Arrays.fill(lut, -1); 
         
        // at first loop all pixels will move their clusters 
        boolean pixelChangedCluster = true; 
        // loop until all clusters are stable! 
        int loops = 0; 
        while (pixelChangedCluster) { 
            pixelChangedCluster = false; 
            loops++; 
            for (int y=0;y<h;y++) { 
                for (int x=0;x<w;x++) { 
                    int pixel = image.getRGB(x, y); 
                    Cluster cluster = findMinimalCluster(pixel); 
                    if (lut[w*y+x]!=cluster.getId()) { 
                        // cluster changed 
                        if (mode==MODE_CONTINUOUS) { 
                            if (lut[w*y+x]!=-1) { 
                                // remove from possible previous  
                                // cluster 
                                clusters[lut[w*y+x]].removePixel( 
                                                            pixel); 
                            } 
                            // add pixel to cluster 
                            cluster.addPixel(pixel); 
                        } 
                        // continue looping  
                        pixelChangedCluster = true; 
                     
                        // update lut 
                        lut[w*y+x] = cluster.getId(); 
                    } 
                } 
            } 
            if (mode==MODE_ITERATIVE) { 
                // update clusters 
                for (int i=0;i<clusters.length;i++) { 
                    clusters[i].clear(); 
                } 
                for (int y=0;y<h;y++) { 
                    for (int x=0;x<w;x++) { 
                        int clusterId = lut[w*y+x]; 
                        // add pixels to cluster 
                        clusters[clusterId].addPixel( 
                                            image.getRGB(x, y)); 
                    } 
                } 
            } 
             
        } 
        // create result image 
        BufferedImage result = new BufferedImage(w, h,  
                                    BufferedImage.TYPE_INT_RGB); 
        for (int y=0;y<h;y++) { 
            for (int x=0;x<w;x++) { 
                int clusterId = lut[w*y+x]; 
                result.setRGB(x, y, clusters[clusterId].getRGB()); 
            } 
        } 
        long end = System.currentTimeMillis(); 
        System.out.println("Clustered to "+k 
                            + " clusters in "+loops 
                            +" loops in "+(end-start)+" ms."); 
        String t = String.valueOf(end-start);
        jTextField2.setText(t);
        
        return result; 
    } 
     
    public Cluster[] createClusters(BufferedImage image, int k) { 
        // Here the clusters are taken with specific steps, 
        // so the result looks always same with same image. 
        // You can randomize the cluster centers, if you like. 
        Cluster[] result = new Cluster[k]; 
        int x = 0; int y = 0; 
        int dx = image.getWidth()/k; 
        int dy = image.getHeight()/k; 
        for (int i=0;i<k;i++) { 
            result[i] = new Cluster(i,image.getRGB(x, y)); 
            x+=dx; y+=dy; 
        } 
        return result; 
    } 
     
    public Cluster findMinimalCluster(int rgb) { 
        Cluster cluster = null; 
        int min = Integer.MAX_VALUE; 
        for (int i=0;i<clusters.length;i++) { 
            int distance = clusters[i].distance(rgb); 
            if (distance<min) { 
                min = distance; 
                cluster = clusters[i]; 
            } 
        } 
        return cluster; 
    } 
     
  
    
    public  BufferedImage loadImage(String filename) { 
        BufferedImage result = null; 
        try { 
            result = ImageIO.read(new File(filename)); 
        } catch (Exception e) { 
            System.out.println(e.toString()+" Image '" 
                                +filename+"' not found."); 
        } 
        return result; 
    } 
     
    class Cluster { 
        int id; 
        int pixelCount; 
        int red; 
        int green; 
        int blue; 
        int reds; 
        int greens; 
        int blues; 
         
        public Cluster(int id, int rgb) { 
            int r = rgb>>16&0x000000FF;  
            int g = rgb>> 8&0x000000FF;  
            int b = rgb>> 0&0x000000FF;  
            red = r; 
            green = g; 
            blue = b; 
            this.id = id; 
            addPixel(rgb); 
        } 
         
        public void clear() { 
            red = 0; 
            green = 0; 
            blue = 0; 
            reds = 0; 
            greens = 0; 
            blues = 0; 
            pixelCount = 0; 
        } 
         
        int getId() { 
            return id; 
        } 
         
        int getRGB() { 
            int r = reds / pixelCount; 
            int g = greens / pixelCount; 
            int b = blues / pixelCount; 
            return 0xff000000|r<<16|g<<8|b; 
        } 
        void addPixel(int color) { 
            int r = color>>16&0x000000FF;  
            int g = color>> 8&0x000000FF;  
            int b = color>> 0&0x000000FF;  
            reds+=r; 
            greens+=g; 
            blues+=b; 
            pixelCount++; 
            red   = reds/pixelCount; 
            green = greens/pixelCount; 
            blue  = blues/pixelCount; 
        } 
         
        void removePixel(int color) { 
            int r = color>>16&0x000000FF;  
            int g = color>> 8&0x000000FF;  
            int b = color>> 0&0x000000FF;  
            reds-=r; 
            greens-=g; 
            blues-=b; 
            pixelCount--; 
            red   = reds/pixelCount; 
            green = greens/pixelCount; 
            blue  = blues/pixelCount; 
        } 
         
        int distance(int color) { 
            int r = color>>16&0x000000FF;  
            int g = color>> 8&0x000000FF;  
            int b = color>> 0&0x000000FF;  
            int rx = Math.abs(red-r); 
            int gx = Math.abs(green-g); 
            int bx = Math.abs(blue-b); 
            int d = (rx+gx+bx) / 3; 
            return d; 
        } 
    } 
     
}

    String c,c1,c2 = "";
    BufferedImage img;
    BufferedImage img1;
    BufferedImage img2;
    BufferedImage img3;
    int comp1=0,comp2=0,comp3=0,comp4=0,comp5=0,comp6=0;
    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        this.setTitle("application de segmentation d'images irm");
        this.setVisible(true);
        
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false); 
        /*Image icone = Toolkit.getDefaultToolkit().getImage("C:\\Users\\laptop\\Desktop\\netbeans.png");
        this.setIconImage(icone);*/
        initComponents();
        this.setSize(1076,665);
        this.setLocationRelativeTo(null);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);
        jButton5.setEnabled(false);
        jButton6.setEnabled(false);
        jButton7.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel1.setLayout(null);

        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel1MouseEntered(evt);
            }
        });
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 300, 250);

        getContentPane().add(jPanel1);
        jPanel1.setBounds(380, 50, 300, 250);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel2.setPreferredSize(new java.awt.Dimension(300, 250));
        jPanel2.setLayout(null);
        jPanel2.add(jLabel2);
        jLabel2.setBounds(0, 0, 300, 250);

        getContentPane().add(jPanel2);
        jPanel2.setBounds(30, 370, 300, 250);

        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 250));
        jPanel3.setLayout(null);
        jPanel3.add(jLabel3);
        jLabel3.setBounds(0, 0, 300, 250);

        getContentPane().add(jPanel3);
        jPanel3.setBounds(750, 370, 301, 250);

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Times New Roman", 0, 11)); // NOI18N
        jButton1.setText("parcourir");
        jButton1.setBorder(null);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(280, 10, 80, 30);

        jTextField1.setText("c:/");
        jTextField1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        getContentPane().add(jTextField1);
        jTextField1.setBounds(390, 10, 287, 20);

        jButton2.setBackground(new java.awt.Color(153, 153, 153));
        jButton2.setText("sobel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(70, 320, 85, 23);

        jButton4.setBackground(new java.awt.Color(153, 153, 153));
        jButton4.setText("enregistrer");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4);
        jButton4.setBounds(180, 320, 85, 23);

        jButton3.setBackground(new java.awt.Color(153, 153, 153));
        jButton3.setText("perwitt");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3);
        jButton3.setBounds(430, 320, 85, 23);

        jButton5.setBackground(new java.awt.Color(153, 153, 153));
        jButton5.setText("enregistrer");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5);
        jButton5.setBounds(540, 320, 85, 23);

        jButton6.setText("k-means");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6);
        jButton6.setBounds(790, 320, 90, 23);

        jButton7.setText("enregistrer");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton7);
        jButton7.setBounds(910, 320, 85, 23);

        jLabel5.setText("introduire le nombre de classe");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(760, 230, 170, 30);
        getContentPane().add(jTextField3);
        jTextField3.setBounds(960, 230, 50, 20);

        jLabel6.setText("temp d'exécution en ms");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(770, 270, 120, 14);
        getContentPane().add(jTextField2);
        jTextField2.setBounds(960, 270, 50, 20);

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel4.setLayout(null);
        jPanel4.add(jLabel7);
        jLabel7.setBounds(0, 0, 300, 250);

        getContentPane().add(jPanel4);
        jPanel4.setBounds(380, 370, 300, 250);

        jLabel4.setIcon(new javax.swing.ImageIcon("C:\\Users\\laptop\\Documents\\NetBeansProjects\\JavaApplication\\src\\javaapplication\\imgdefond.jpg")); // NOI18N
        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(0, 0, 1080, 660);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        JFileChooser chooser = new JFileChooser();//création dun nouveau filechosser
	chooser.setApproveButtonText("ok"); //intitulé du bouton
        chooser.setDialogTitle("choix de l'image");
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	        {
		c = chooser.getSelectedFile().getAbsolutePath().toString();
		jTextField1.setText(c);
                //pour redimensionner l'image
                ImageIcon icon = new ImageIcon(c); 
                Image scaleImage = icon.getImage().getScaledInstance(300, 250,Image.SCALE_DEFAULT);               
                jLabel1.setIcon(new javax.swing.ImageIcon(scaleImage));
                comp1 = comp1+1;
                 jButton2.setEnabled(true);
                  jButton3.setEnabled(true);
                  jButton6.setEnabled(true);
                }
      
       
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jLabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseEntered
        if (comp1 !=0)
        jLabel1.setToolTipText("cliquer ici pour agrandir l'image");          
    }//GEN-LAST:event_jLabel1MouseEntered

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         
      
    
   /*ImageIcon icon = new ImageIcon(c); 
                Image scaleImage = icon.getImage().getScaledInstance(300, 250,Image.SCALE_DEFAULT);               
                jLabel2.setIcon(new javax.swing.ImageIcon(scaleImage));
                comp1 = comp1+1;*/
      	// Définition d'un objet BufferedImage
         Image imgredim;
          
           /* Image imgredim;     
            
    try {
            //Ouverture du fichier
            File inputFile = new File(c);
            //c1 = c.substring(0,c.length()-5);
            //c1 = c1 +"1.jpeg";
            BufferedImage imagesrc = ImageIO.read(inputFile);
             //Convertion en grisé
             imagedst = new BufferedImage(imagesrc.getWidth(),imagesrc.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
             
             Graphics g = imagedst.getGraphics();
             g.drawImage(imagesrc, 0, 0, null);
             g.dispose();
             imgredim= imagedst.getScaledInstance(300,250, imagedst.SCALE_DEFAULT);
             // ImageIcon icon = new ImageIcon(c1); 
            //Image scaleImage = icon.getImage().getScaledInstance(300, 250,Image.SCALE_DEFAULT);               
            jLabel2.setIcon(new javax.swing.ImageIcon(imgredim));
            comp2 = comp2+1;
            jButton4.setEnabled(true);
        } catch (IOException ex) {
           // Logger.getLogger(ImageGrayScale.class.getName()).log(Level.SEVERE, null, ex);
        }*/
               
        try {
    img=ImageIO.read(new File(c));
    int [][] pixel= new int[img.getWidth()][img.getHeight()];
    int x,y,g;
    //**********Conversion en niveau du Gris************
    for (int i = 0; i < img.getWidth(); i++) {
        for (int j = 0; j < img.getHeight(); j++) {
             Color pixelcolor= new Color(img.getRGB(i,j));
             int r=pixelcolor.getRed();
             int gb=pixelcolor.getGreen();
             int b=pixelcolor.getBlue();
             int hy=(r+gb+b)/3;
             
             
             int rgb = new Color(hy,hy,hy).getRGB();
            
             //************changer la couleur de pixel avec la nouvelle couleur inversée**********
             img.setRGB(i, j, rgb);
        }
    }    
       //***********parcourir les pixels de l'image***********
    for (int i = 0; i < img.getWidth(); i++) 
    {
        for (int j = 0; j < img.getHeight(); j++) 
        {
        // recuperer couleur de chaque pixel
        Color pixelcolor= new Color(img.getRGB(i, j));
        // recuperer les valeur rgb (rouge ,vert ,bleu) de cette couleur
         pixel[i][j]=img.getRGB(i, j);
        }
    }
//*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*

    for (int i = 1; i < img.getWidth()-2; i++) 
    {
        for (int j = 1; j < img.getHeight()-2; j++) 
        {
            
        x=(pixel[i][j+2]+
                
         2*pixel[i+1][j+2]+
                
         pixel[i+2][j+2])-
                
         (pixel[i][j]+
                
          2*pixel[i+1][j]+
                
          pixel[i+2][j]);
        
        
        y = (pixel[i + 2][j] + 
                
          2 * pixel[i + 2][j + 1] + 
                
          pixel[i + 2][j + 2]) - 
                
          (pixel[i][j] + 
                
           2 * pixel[i][j + 1] + 
                
           pixel[i][j + 2]);
        
        g=Math.abs(x)+Math.abs(y);    
        //System.out.println(g);
        pixel[i][j]=g;
        }
    }

    //**********************************************************************************    
        
     for (int i = 0; i < img.getWidth(); i++) {
        for (int j = 0; j < img.getHeight(); j++) {
        Color pixelcolor= new Color(pixel[i][j]);
        int r=pixelcolor.getRed();
        int gb=pixelcolor.getGreen();
        int b=pixelcolor.getBlue();
        int rgb=new Color(r,gb,b).getRGB();
        // changer la couleur de pixel avec la nouvelle couleur inversée
        img.setRGB(i, j, rgb);

        }
    }    
    // enregistrement d'image
        //ImageIO.write(img, "bmp",new File("Sobel.bmp"))    ;//ImageIO.write()//;
        imgredim= img.getScaledInstance(300,250, img.SCALE_DEFAULT);
        jLabel2.setIcon(new javax.swing.ImageIcon(imgredim));
        comp2 = comp2+1;
        jButton4.setEnabled(true);
    }
    catch (Exception e) {
        System.err.println("erreur -> "+e.getMessage());
    } 
                     
           
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Image imgredim;
        
        
         // ImageIcon icon = new ImageIcon(c); 
         //       Image scaleImage = icon.getImage().getScaledInstance(300, 250,Image.SCALE_DEFAULT);               
           //     jLabel3.setIcon(new javax.swing.ImageIcon(scaleImage));
            
           /* try {
            //Ouverture du fichier
            File inputFile = new File(c);
            //c1 = c.substring(0,c.length()-5);
            //c1 = c1 +"1.jpeg";
            BufferedImage imagesrc = ImageIO.read(inputFile);
            
           // Color couleur = Color.red;
           // Color couleur1 = Color.green;
           
            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),null);
            imagedst1  = op.filter(imagesrc,null);
           
            Graphics g = imagedst1.createGraphics();
            g.drawImage(imagedst1,0,0,null);
            g.dispose();
            imgredim = imagedst1.getScaledInstance(300,250, imagedst1.SCALE_DEFAULT);
             
             //pour convertir de image a bufferedimage
              //imagedst1 = new BufferedImage(imgredim.getWidth(null),imgredim.getHeight(null),BufferedImage.TYPE_INT_RGB );
              
              
             
             
             
             
            /* for (i=0;i<imagedst.getWidth();i++)
                 if ((i% 2)==0){
                for(j=0;j<imagedst.getHeight();j++)
                    if ((j% 2)==0)
                   imagedst.setRGB(i, j, 100);}
                 else{
                     for(j=0;j<imagedst.getHeight();j++)
                         if ((j% 2)!=0)
                   imagedst.setRGB(i, j, 0);
                 }*/
           
             // ImageIcon icon = new ImageIcon(c1); 
            //Image scaleImage = icon.getImage().getScaledInstance(300, 250,Image.SCALE_DEFAULT);               
           /* jLabel3.setIcon(new javax.swing.ImageIcon(imgredim));
                comp3=comp3+1;
                jButton5.setEnabled(true);
                } catch (IOException ex) {
           //Logger.getLogger(ImageGrayScale.class.getName()).log(Level.SEVERE, null, ex);
        }*/
      try {
         img1=ImageIO.read(new File(c));
         int [][] pixel= new int[img1.getWidth()][img1.getHeight()];
         int x,y,g;
         //***********Conversion enniveau du Gris**************
        for (int i = 0; i < img1.getWidth(); i++) {
        for (int j = 0; j < img1.getHeight(); j++) {
        Color pixelcolor= new Color(img1.getRGB(i,j));
        int r=pixelcolor.getRed();
        int gb=pixelcolor.getGreen();
        int b=pixelcolor.getBlue();
        int hy=(r+gb+b)/3;
        int rgb=new Color(hy,hy,hy).getRGB();
        //**************changer la couleur de pixel avec la nouvelle couleur inversée***********
        img1.setRGB(i, j, rgb);
        }
    }    
       //**************parcourir les pixels de l'image**********************
    for (int i = 0; i < img1.getWidth(); i++) 
    {
        for (int j = 0; j < img1.getHeight(); j++) 
        {
         // recuperer couleur de chaque pixel
        Color pixelcolor= new Color(img1.getRGB(i, j));
         // recuperer les valeur rgb (rouge ,vert ,bleu) de cette couleur
         pixel[i][j]=img1.getRGB(i, j);
        }
    }

//*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*

    for (int i = 1; i < img1.getWidth()-2; i++) 
    {
        for (int j = 1; j < img1.getHeight()-2; j++) 
        {
            
//pixel[i][j]=(pixel[i-1][j-1]+pixel[i-1][j]+pixel[i-1][j+1]+pixel[i][j-1]+pixel[i][j]+pixel[i][j+1]+pixel[i+1][j-1]+pixel[i+1][j]+pixel[i+1][j+1])/9;            
            
x=(pixel[i][j+2]+
        
   pixel[i+1][j+2]+
        
   pixel[i+2][j+2])-
        
        (pixel[i][j]+
        
        pixel[i+1][j]+
        
        pixel[i+2][j]);


y = (pixel[i + 2][j] + 
        
     pixel[i + 2][j + 1] + 
        
     pixel[i + 2][j + 2]) - 
        
        (pixel[i][j] +  
        
         pixel[i][j + 1] + 
         pixel[i][j + 2]);
g=Math.abs(x)+Math.abs(y);    


//System.out.println(g);
pixel[i][j]=g;

    }
    }
//*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
        for (int i = 0; i < img1.getWidth(); i++)
        {
        for (int j = 0; j < img1.getHeight(); j++) 
        {
        Color pixelcolor= new Color(pixel[i][j]);
        int r=pixelcolor.getRed();
        int gb=pixelcolor.getGreen();
        int b=pixelcolor.getBlue();
        int rgb=new Color(r,gb,b).getRGB();
        // changer la couleur de pixel avec la nouvelle couleur inversée
        img1.setRGB(i, j, rgb);

        }
    }    
    // enregistrement d'image
        //ImageIO.write(img1, "bmp",new File("prewitt.bmp"))    ;//ImageIO.write()//;
        imgredim= img1.getScaledInstance(300,250, img1.SCALE_DEFAULT);
        jLabel7.setIcon(new javax.swing.ImageIcon(imgredim));
        comp3 = comp3+1;
        jButton5.setEnabled(true);
      }
    catch (Exception e) {
        System.err.println("erreur -> "+e.getMessage());
    }
    
    

        
                
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
     String c;
    dialogue d;
    if (comp2 != 0){
        JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new java.io.File("."));
      chooser.setDialogTitle("enregistrer");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(true);
   //FileFilter imagesFilter = new FileNameExtensionFilter("Images bmp", ".bmp");
   //FileFilter imagesFilter1 = new FileNameExtensionFilter("Images jpg", ".jpg");
   
   //chooser.addChoosableFileFilter(imagesFilter);
   //chooser.addChoosableFileFilter(imagesFilter1);
       
      

   if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
      System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
           //File outFile1 = new File(c1);
         try {
             //Enregistrer l'image au format jpeg
             ImageIO.write(img, "jpeg", chooser.getSelectedFile());
              d = new dialogue();
              c1 = chooser.getSelectedFile().toString();
              comp4=comp4+1;
         } catch (IOException ex) {
             Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
   }
      
      
    } else {
      System.out.println("No Selection ");
    }
        
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        
    dialogue d;
    if (comp3 != 0){
        JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new java.io.File("."));
      chooser.setDialogTitle("enregistrer");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(true);
   //FileFilter imagesFilter = new FileNameExtensionFilter("Images bmp", ".bmp");
   //FileFilter imagesFilter1 = new FileNameExtensionFilter("Images jpg", ".jpg");
   
   //chooser.addChoosableFileFilter(imagesFilter);
   //chooser.addChoosableFileFilter(imagesFilter1);
       
      

   if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
      System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
           //File outFile1 = new File(c1);
         try {
             //Enregistrer l'image au format jpeg
             ImageIO.write(img1, "jpeg", chooser.getSelectedFile());
              d = new dialogue();
              c2 = chooser.getSelectedFile().toString();
              comp5=comp5+1;
         } catch (IOException ex) {
            // Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
   }
      
      
    } else {
      System.out.println("No Selection ");
    }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked

        
        if (comp1 !=0){
            
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec(new String[] { "C:\\Program Files (x86)\\Picexa\\Picexa.exe", ""+c} );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        }

	

    }//GEN-LAST:event_jLabel1MouseClicked

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        
        
       Image imgredim;
        
       
 
        int k = Integer.parseInt(jTextField3.getText()); 
        String m = "-c"; 
        int mode = 1; 
        if (m.equals("-i")) { 
            mode = MODE_ITERATIVE; 
        } else if (m.equals("-c")) { 
            mode = MODE_CONTINUOUS; 
        } 
        
        
        // create new KMeans object 
        kmeans kmeans = new kmeans(); 
        // call the function to actually start the clustering 
        BufferedImage dstImage = kmeans.calculate(kmeans.loadImage(c), 
                                                    k,mode); 
        // save the resulting image 
      
      img2 = dstImage;
      imgredim= img2.getScaledInstance(300,250, img2.SCALE_DEFAULT);
      jLabel3.setIcon(new javax.swing.ImageIcon(imgredim));
      comp6 = comp6+1;
      jButton7.setEnabled(true);
    
     
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        
        
         dialogue d;
    if (comp6 != 0){
        JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new java.io.File("."));
      chooser.setDialogTitle("enregistrer");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(true);
   //FileFilter imagesFilter = new FileNameExtensionFilter("Images bmp", ".bmp");
   //FileFilter imagesFilter1 = new FileNameExtensionFilter("Images jpg", ".jpg");
   
   //chooser.addChoosableFileFilter(imagesFilter);
   //chooser.addChoosableFileFilter(imagesFilter1);
       
      

   if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
      System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
           //File outFile1 = new File(c1);
         try {
             //Enregistrer l'image au format jpeg
             ImageIO.write(img2, "jpeg", chooser.getSelectedFile());
              d = new dialogue();
              c2 = chooser.getSelectedFile().toString();
              comp5=comp5+1;
         } catch (IOException ex) {
            // Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
   }
      
      
    } else {
      System.out.println("No Selection ");
    }
        
        
        
        
        
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
