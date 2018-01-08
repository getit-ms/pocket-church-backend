package br.gafs.calvinista.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Gabriel on 26/11/2017.
 */
public class PDFToImageConverterUtilTest {

    @Test
    @Ignore
    public void testConversao() throws IOException, URISyntaxException {
        // load a pdf from a byte buffer
        File file = new File("teste.pdf");

        PDFToImageConverterUtil.PDFConverter converter = new PDFToImageConverterUtil.PDFConverter(file, 0, 5);

        converter.forEachPage(new PDFToImageConverterUtil.PageHandler() {
            @Override
            public void handle(int page, byte[] data) throws IOException {

                File file = new File("pagina_"+page+".png");

                FileOutputStream fos = new FileOutputStream(file);

                fos.write(data);

                fos.close();

            }
        });


    }


}