/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package mergepatch;

import com.melody.mergepatch.Main;
import org.junit.Test;

public class AppTest {
    @Test public void mergepatch() {
        String[] args = new String[3];
//        args[1] = "../assets/source";
//        args[2] = "../assets/target.patch";
//        args[1] = "../assets/pdf/source.pdf";
//        args[2] = "../assets/pdf/target.pdf.patch";
        args[1] = "../assets/source.apk";
        args[2] = "../assets/target.apk.patch";
        Main.main(args);
    }
}
