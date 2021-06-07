package makepatch;

import com.melody.util.ExecUtil;
import com.melody.util.Util;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ExecUtilTest {

    @Test
    public void checkMd5Consume(){
        ExecUtil.exec(()->{
            String checksum = "5EB63BBBE01EEED093CB22BB8F5ACDC3";

            byte[] source = new byte[]{21,32,43, -12, 45, 43,21, 99};

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            md.update(source);
            byte[] digest = md.digest();
            String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
        });
    }


    @Test
    public void checkCrc32Consume(){
        ExecUtil.exec(()->{

            byte[] source = new byte[]{21,32,43, -12, 45, 43,21, 99};

            Arrays.hashCode(source);

        });
    }
}
