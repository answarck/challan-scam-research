import java.io.*;
import java.util.zip.GZIPInputStream;

public class Poc {

    public static void main(String[] args) throws IOException {

        File dexDir = new File("p_dex");
        dexDir.mkdirs();

        DataInputStream stream = new DataInputStream(
            new FileInputStream("pyxMdfyeES")
        );

        stream.skipBytes(24);

        int count = stream.readInt();

        StringBuilder dexPaths = new StringBuilder();

        for (int i = 0; i < count; ++i) {

            int size = stream.readInt();
            String filename = "payload_" + i + ".dex";
            File dexFile = new File(dexDir, filename);

            if (dexFile.exists()) {
                dexFile.setWritable(true);
                dexFile.delete();
            }

            byte[] encrypted = new byte[size];

            for (int j = 0; j < size; ++j) {
                encrypted[j] = (byte)(
                    (stream.readByte() ^ 87) - 25 ^ 242
                );
            }

            ByteArrayInputStream byteStream = new ByteArrayInputStream(encrypted);

            GZIPInputStream gzip = new GZIPInputStream(byteStream);

            try (FileOutputStream output = new FileOutputStream(dexFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = gzip.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                output.flush();
                output.getFD().sync();
            }

            gzip.close();

            if (dexPaths.length() > 0) {
                dexPaths.append(File.pathSeparator);
            }

            dexPaths.append(dexFile.getAbsolutePath());
        }

        stream.close();
    }
}