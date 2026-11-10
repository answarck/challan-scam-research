import java.io.*;
import java.util.zip.GZIPInputStream;

public class Poc {
    public static void main(String[] args) throws IOException {

        String inp = "YUVQNhVkYqm";

        File output_dir = new File("assets_i");
        output_dir.mkdirs();

        DataInputStream stream = new DataInputStream(
            new FileInputStream(inp)
        );

        stream.skipBytes(24);

        int size = stream.readInt();
        byte[] final_bytes = new byte[size];

        for (int i = 0; i < size; ++i) {
            final_bytes[i] = (byte)(
                (stream.readByte() ^ 132) - 218 ^ 167
            );
        }

        ByteArrayInputStream byte_array =
            new ByteArrayInputStream(final_bytes);

        GZIPInputStream gzip_stream =
            new GZIPInputStream(byte_array);

        DataInputStream data_stream =
            new DataInputStream(gzip_stream);

        int gzip_size = data_stream.readInt();

        for (int i = 0; i < gzip_size; i++) {

            int name_length = data_stream.readInt();

            byte[] filename_bytes = new byte[name_length];
            data_stream.readFully(filename_bytes);

            String filename = new String(filename_bytes, "UTF-8");

            File output_file = new File(output_dir, filename);

            output_file.getParentFile().mkdirs();

            try (FileOutputStream out_file =
                    new FileOutputStream(output_file)) {

                byte[] buffer = new byte[8192];

                for (int remaining = data_stream.readInt();
                     remaining > 0;) {

                    int chunk = Math.min(8192, remaining);

                    data_stream.readFully(buffer, 0, chunk);
                    out_file.write(buffer, 0, chunk);

                    remaining -= chunk;
                }
            }
        }

        data_stream.close();
        stream.close();
    }
}