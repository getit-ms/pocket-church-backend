package br.gafs.calvinista.app.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mirante0 on 01/02/2017.
 */
public class ArquivoUtil {
    private final static Logger LOGGER = Logger.getLogger(ArquivoUtil.class.getName());


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
}
