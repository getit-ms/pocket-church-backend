package br.gafs.pocket.corporate.app.util;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mirante0 on 01/02/2017.
 */
public class ArquivoUtil {
    private final static Logger LOGGER = Logger.getLogger(ArquivoUtil.class.getName());
    public static final int BUFF_LIMIT = 1024 * 1024 * 2;

    public static void transfer(long from, long to, File file, OutputStream outputStream, int buffSize) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(file, "r");

        raf.seek(from);

        long len = (to - from) + 1;

        int size;
        byte[] cache = new byte[Math.min(buffSize, BUFF_LIMIT)];
        while (len > 0) {
            size = raf.read(cache, 0, (int) Math.min(len, cache.length));

            outputStream.write(cache, 0, size);

            outputStream.flush();

            len -= size;
        }

        outputStream.close();

        raf.close();
    }

    public static void transfer(InputStream is, OutputStream os){
        try{
            int size;
            byte[] cache = new byte[5000];
            while ((size = is.read(cache)) > 0){
                os.write(cache, 0, size);
            }

            is.close();
            os.close();
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Problema ao transferir dados", e);
        }
    }

    public static void transfer(RandomAccessFile raf, int len, ServletOutputStream outputStream) throws IOException {
        try {
            byte[] cache = new byte[5000];
            int size;
            while (len > 0) {
                size = raf.read(cache, 0, Math.min(len, cache.length));

                outputStream.write(cache, 0,size);

                outputStream.flush();

                if (size < cache.length) {
                    break;
                }

                len -= size;
            }
        } finally {
            raf.close();
        }
    }
}
