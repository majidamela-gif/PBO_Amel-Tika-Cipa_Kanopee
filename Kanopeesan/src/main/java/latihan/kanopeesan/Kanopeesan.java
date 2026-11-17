/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package latihan.kanopeesan;
import latihan.db.DatabaseInitializer;
import latihan.view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author ameld
 */
public class Kanopeesan {

    public static void main(String[] args) {
        try {
            for (
                    UIManager.LookAndFeelInfo info
                    : UIManager.getInstalledLookAndFeels()
            ) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(
                            info.getClassName()
                    );
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Buat database + tabel + data awal
        DatabaseInitializer.initialize();

        // Buka halaman login
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame =
                    new LoginFrame();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }
}
