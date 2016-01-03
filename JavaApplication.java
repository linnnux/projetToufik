
package javaapplication;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class JavaApplication {
    public static void main(String[] args) {
        
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    
                	NewJFrame fen = new NewJFrame();
                       //contour  http://codes-sources.commentcamarche.net/source/view/50908/1207505#browser
                }
            });
        } catch (Exception ex) {
          
            //Erreur inconnnue ou de look and feel
        }

		
	}
    }
    

